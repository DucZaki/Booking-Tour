package edu.bookingtour.service;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.repo.DatChoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CheckInService {

    private final DatChoRepository datChoRepository;

    public CheckInService(DatChoRepository datChoRepository) {
        this.datChoRepository = datChoRepository;
    }

    public Optional<DatCho> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return datChoRepository.findByMaCheckInWithDetails(token.trim());
    }

    /** Gán token nếu đơn chưa có (đơn cũ trước migration). */
    @Transactional
    public String ensureCheckInToken(DatCho booking) {
        if (booking.getMaCheckIn() != null && !booking.getMaCheckIn().isBlank()) {
            return booking.getMaCheckIn();
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        booking.setMaCheckIn(token);
        datChoRepository.save(booking);
        return token;
    }

    @Transactional
    public CheckInResult confirmCheckIn(String token) {
        DatCho booking = datChoRepository.findByMaCheckInWithDetails(token.trim())
                .orElse(null);
        if (booking == null) {
            return CheckInResult.ofNotFound();
        }
        if (!"PAID".equals(booking.getTrangThai())) {
            return CheckInResult.invalid("Đơn chưa thanh toán — không thể check-in.");
        }
        if (booking.getCheckedInAt() != null) {
            return CheckInResult.ofAlreadyCheckedIn(booking);
        }
        booking.setCheckedInAt(LocalDateTime.now());
        datChoRepository.save(booking);
        return CheckInResult.ofSuccess(booking);
    }

    public record CheckInResult(
            boolean ok,
            boolean notFound,
            boolean alreadyCheckedIn,
            String message,
            DatCho booking) {

        static CheckInResult ofNotFound() {
            return new CheckInResult(false, true, false, "Không tìm thấy vé.", null);
        }

        static CheckInResult invalid(String msg) {
            return new CheckInResult(false, false, false, msg, null);
        }

        static CheckInResult ofAlreadyCheckedIn(DatCho b) {
            return new CheckInResult(false, false, true, "Khách đã check-in trước đó.", b);
        }

        static CheckInResult ofSuccess(DatCho b) {
            return new CheckInResult(true, false, false, "Check-in thành công!", b);
        }
    }
}
