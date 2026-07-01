package com.re.badmintonsystem.config;

import com.re.badmintonsystem.entity.*;
import com.re.badmintonsystem.entity.enums.CourtStatus;
import com.re.badmintonsystem.entity.enums.UserStatus;
import com.re.badmintonsystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      CourtRepository courtRepository,
                      CourtImageRepository courtImageRepository,
                      TimeSlotRepository timeSlotRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.courtRepository = courtRepository;
        this.courtImageRepository = courtImageRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data. Skipping seed.");
            return;
        }

        log.info("Database is empty. Starting data seed...");

        log.info("[1/4] Seeding Roles...");
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Quản trị viên hệ thống")));
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(new Role("MANAGER", "Quản lý sân")));
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("CUSTOMER", "Khách hàng đặt sân")));
        log.info("  Roles ready: ADMIN, MANAGER, CUSTOMER");

        log.info("[2/4] Seeding Users...");
        String defaultPassword = passwordEncoder.encode("123456");

        User admin = createUser("admin", "admin@badminton.com", defaultPassword, "Quản trị viên", "0900000001", Set.of(adminRole));
        User manager = createUser("manager", "manager@badminton.com", defaultPassword, "Quản lý sân 1", "0900000002", Set.of(managerRole));
        User customer1 = createUser("customer", "customer@badminton.com", defaultPassword, "Khách hàng 1", "0900000003", Set.of(customerRole));
        User customer2 = createUser("customer2", "customer2@badminton.com", defaultPassword, "Khách hàng 2", "0900000004", Set.of(customerRole));

        admin = userRepository.save(admin);
        manager = userRepository.save(manager);
        customer1 = userRepository.save(customer1);
        customer2 = userRepository.save(customer2);
        log.info("  Created users: admin, manager, customer, customer2 (password: 123456)");

        log.info("[3/4] Seeding Courts...");

        Court court1 = createCourt("SÂN-01", "Sân cầu lông VIP 1", "Sân cao cấp, sàn gỗ, máy lạnh", new BigDecimal("150000"), manager);
        Court court2 = createCourt("SÂN-02", "Sân cầu lông VIP 2", "Sân cao cấp, sàn gỗ, máy lạnh", new BigDecimal("150000"), manager);
        Court court3 = createCourt("SÂN-03", "Sân cầu lông Tiêu chuẩn 1", "Sân tiêu chuẩn, đèn LED", new BigDecimal("100000"), manager);
        Court court4 = createCourt("SÂN-04", "Sân cầu lông Tiêu chuẩn 2", "Sân tiêu chuẩn, đèn LED", new BigDecimal("100000"), manager);
        Court court5 = createCourt("SÂN-05", "Sân cầu lông Thường 1", "Sân thường phong trào", new BigDecimal("80000"), manager);

        court1 = courtRepository.save(court1);
        court2 = courtRepository.save(court2);
        court3 = courtRepository.save(court3);
        court4 = courtRepository.save(court4);
        court5 = courtRepository.save(court5);
        log.info("  Created 5 courts");

        log.info("  Seeding Court Images...");
        courtImageRepository.save(createCourtImage(court1, "https://res.cloudinary.com/demo/image/upload/v1/badminton/court-vip-1", "badminton/court-vip-1", 0));
        courtImageRepository.save(createCourtImage(court2, "https://res.cloudinary.com/demo/image/upload/v1/badminton/court-vip-2", "badminton/court-vip-2", 0));
        courtImageRepository.save(createCourtImage(court3, "https://res.cloudinary.com/demo/image/upload/v1/badminton/court-std-1", "badminton/court-std-1", 0));
        log.info("  Created 3 court images (demo)");

        log.info("[4/4] Seeding Time Slots...");
        seedTimeSlots(timeSlotRepository);
        log.info("  Created 16 time slots (05:00 - 22:00)");

        log.info("========================================");
        log.info(" Data seed completed successfully!");
        log.info("========================================");
        log.info(" Test accounts:");
        log.info("  Admin:    admin     / 123456");
        log.info("  Manager:  manager   / 123456");
        log.info("  Customer: customer  / 123456");
        log.info("========================================");
    }

    private User createUser(String username, String email, String passwordHash,
                            String fullName, String phone, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);
        return user;
    }

    private Court createCourt(String courtCode, String name, String description,
                              BigDecimal basePricePerHour, User manager) {
        Court court = new Court();
        court.setCourtCode(courtCode);
        court.setName(name);
        court.setDescription(description);
        court.setBasePricePerHour(basePricePerHour);
        court.setStatus(CourtStatus.ACTIVE);
        court.setManager(manager);
        return court;
    }

    private CourtImage createCourtImage(Court court, String imageUrl, String publicId, int displayOrder) {
        CourtImage image = new CourtImage();
        image.setCourt(court);
        image.setImageUrl(imageUrl);
        image.setPublicId(publicId);
        image.setDisplayOrder(displayOrder);
        return image;
    }

    private void seedTimeSlots(TimeSlotRepository repo) {
        int[][] slots = {
                {5, 6}, {6, 7}, {7, 8}, {8, 9}, {9, 10}, {10, 11}, {11, 12},
                {13, 14}, {14, 15}, {15, 16}, {16, 17}, {17, 18},
                {18, 19}, {19, 20}, {20, 21}, {21, 22}
        };

        for (int[] slot : slots) {
            LocalTime start = LocalTime.of(slot[0], 0);
            LocalTime end = LocalTime.of(slot[1], 0);

            if (!repo.existsByStartTimeAndEndTime(start, end)) {
                TimeSlot ts = new TimeSlot();
                ts.setStartTime(start);
                ts.setEndTime(end);
                ts.setLabel(String.format("%02d:00 - %02d:00", slot[0], slot[1]));
                repo.save(ts);
            }
        }
    }
}
