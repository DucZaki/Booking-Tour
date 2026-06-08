package edu.bookingtour.config;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.util.DepartureStatusUtil;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "guide.demo.seed.enabled", havingValue = "true")
@Order(4)
public class GuideDemoSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GuideDemoSeedRunner.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final NguoiDungRepository nguoiDungRepository;
    private final ChuyenDiRepository chuyenDiRepository;
    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final DatChoRepository datChoRepository;
    private final PasswordEncoder passwordEncoder;

    public GuideDemoSeedRunner(NguoiDungRepository nguoiDungRepository,
            ChuyenDiRepository chuyenDiRepository,
            NgayKhoiHanhRepository ngayKhoiHanhRepository,
            DatChoRepository datChoRepository,
            PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.chuyenDiRepository = chuyenDiRepository;
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.datChoRepository = datChoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<ChuyenDi> tours = chuyenDiRepository.findAll().stream().limit(3).toList();
        if (tours.size() < 3) {
            log.warn("Không đủ tour để tạo demo GUIDE; cần tối thiểu 3 tour, hiện có {}", tours.size());
            return;
        }

        GuideAccount[] guides = {
                new GuideAccount("guide1", "guide1@zaki.local", "Nguyễn Minh An", "0901000001"),
                new GuideAccount("guide2", "guide2@zaki.local", "Trần Bảo Linh", "0901000002"),
                new GuideAccount("guide3", "guide3@zaki.local", "Lê Hoàng Nam", "0901000003")
        };

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < guides.length; i++) {
            NguoiDung guide = upsertGuide(guides[i]);
            ChuyenDi tour = tours.get(i);
            final int dayOffset = i;
            NgayKhoiHanh departure = ngayKhoiHanhRepository.findByChuyenDiIdAndNgay(tour.getId(), today.plusDays(dayOffset))
                    .orElseGet(() -> createDeparture(tour, today.plusDays(dayOffset)));
            departure.setGuide(guide);
            if (DepartureStatusUtil.isStartTimeReached(departure, now)) {
                departure.setTrangThaiDoanEnum(TrangThaiDoan.IN_PROGRESS);
                departure.setThoiDiemBatDau(now);
            } else {
                departure.setTrangThaiDoanEnum(TrangThaiDoan.SCHEDULED);
                departure.setThoiDiemBatDau(null);
            }
            departure = ngayKhoiHanhRepository.save(departure);

            if (datChoRepository.findPaidManifestByNgayKhoiHanh(departure.getId()).isEmpty()) {
                seedBookings(tour, departure, i);
            }
        }
        log.info("Đã sẵn sàng tài khoản GUIDE demo: guide1/guide2/guide3, mật khẩu {}", DEFAULT_PASSWORD);
    }

    private NguoiDung upsertGuide(GuideAccount account) {
        NguoiDung user = nguoiDungRepository.findByTenDangNhapIgnoreCase(account.username())
                .orElseGet(NguoiDung::new);
        user.setTenDangNhap(account.username());
        user.setEmail(account.email());
        user.setHoTen(account.fullName());
        user.setNumber(account.phone());
        user.setVaiTro("GUIDE");
        if (user.getMatKhau() == null || user.getMatKhau().isBlank()) {
            user.setMatKhau(passwordEncoder.encode(DEFAULT_PASSWORD));
        }
        return nguoiDungRepository.save(user);
    }

    private NgayKhoiHanh createDeparture(ChuyenDi tour, LocalDate date) {
        NgayKhoiHanh departure = new NgayKhoiHanh();
        departure.setChuyenDi(tour);
        departure.setNgay(date);
        departure.setThang(date.getMonthValue());
        departure.setNam(date.getYear());
        departure.setNgayVe(date.plusDays(3));
        departure.setSucChua(tour.getSucChuaMacDinh() != null ? tour.getSucChuaMacDinh() : 40);
        departure.setMaChuyenBayDi("VN" + (720 + date.getDayOfMonth()));
        departure.setGioBayDi("07:30");
        departure.setGioDenDi("09:10");
        edu.bookingtour.util.DepartureTimeUtil.syncGatheringTime(departure, tour);
        departure.setMaChuyenBayVe("VN" + (820 + date.getDayOfMonth()));
        departure.setGioBayVe("18:20");
        departure.setGioDenVe("20:05");
        departure.setTrangThaiDoanEnum(TrangThaiDoan.SCHEDULED);
        return departure;
    }

    private void seedBookings(ChuyenDi tour, NgayKhoiHanh departure, int groupIndex) {
        for (int i = 1; i <= 4; i++) {
            DatCho booking = new DatCho();
            booking.setIdChuyenDi(tour);
            booking.setIdNgayKhoiHanh(departure);
            booking.setIdDiemDon(tour.getIdDiemDon());
            booking.setSoLuong(i == 4 ? 1 : 2);
            booking.setSoNguoiLon(i == 4 ? 1 : 2);
            booking.setNgayDat(LocalDate.now().minusDays(i + groupIndex));
            booking.setCreatedAt(LocalDateTime.now().minusDays(i + groupIndex));
            booking.setTrangThai("PAID");
            booking.setHoTen(sampleName(groupIndex, i));
            booking.setEmail("khach" + (groupIndex + 1) + i + "@demo.local");
            booking.setSoDienThoai("098" + (groupIndex + 1) + "00000" + i);
            booking.setSoGiayTo("07920" + groupIndex + i + "1234");
            booking.setSoGhe("A" + (groupIndex + 1) + i);
            booking.setSoPhong("R" + (groupIndex + 1) + "0" + i);
            booking.setTongGia(tour.getGia() != null ? tour.getGia().doubleValue() * booking.getSoLuong() : 0D);
            booking.setMaCheckIn(UUID.randomUUID().toString().replace("-", ""));
            booking.setCheckinStatusEnum(i == 1 ? CheckInStatus.CHECKED_IN : CheckInStatus.PENDING);
            booking.setGhiChu("Demo: cần nhắc khách có mặt trước giờ tập trung 15 phút.");
            datChoRepository.save(booking);
        }
    }

    private String sampleName(int groupIndex, int index) {
        String[][] names = {
                { "Phạm Quốc Huy", "Đặng Thu Hà", "Võ Gia Bảo", "Ngô Minh Châu" },
                { "Bùi Thanh Tâm", "Hoàng Mỹ Duyên", "Lý Anh Khoa", "Dương Khánh Vy" },
                { "Mai Đức Phúc", "Trịnh Ngọc Lan", "Cao Tuấn Kiệt", "Hồ Bảo Ngọc" }
        };
        return names[groupIndex][index - 1];
    }

    private record GuideAccount(String username, String email, String fullName, String phone) {
    }
}
