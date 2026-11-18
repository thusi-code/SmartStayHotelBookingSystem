package smartstay.service;

import smartstay.model.K2636521BookingStatus;
import smartstay.model.K2636521Invoice;
import smartstay.model.K2636521InvoiceStatus;
import smartstay.model.K2636521Reservation;
import smartstay.util.K2636521InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/* Calculates charges and manages reservation invoices. */
public class K2636521InvoiceService {

    private static final BigDecimal BOOKING_ADVANCE = new BigDecimal("10000.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.12");
    private List<K2636521Invoice> invoices;

    public K2636521InvoiceService() {
        invoices = new ArrayList<>();
    }

    // Generates an invoice for a confirmed reservation.
    public K2636521Invoice generateInvoice(
            K2636521Reservation reservation) {

        if (reservation == null
                || reservation.getRoom() == null
                || reservation.getGuest() == null
                || reservation.getStatus()
                        != K2636521BookingStatus.CONFIRMED
                || reservation.getNumberOfNights() <= 0
                || reservation.getNumberOfGuests() <= 0
                || !reservation.isAdvancePaid()
                || reservation.isAdvanceRefunded()
                || findInvoiceByBookingId(
                        reservation.getBookingId()) != null) {
            return null;
        }

        InvoiceAmounts amounts = calculateInvoiceAmounts(reservation);

        if (amounts == null) {
            return null;
        }

        String invoiceId = generateInvoiceId();

        K2636521Invoice invoice = new K2636521Invoice(invoiceId, reservation,
                amounts.baseCharge, amounts.extraGuestCharge,
                amounts.discount, amounts.taxRate,
                amounts.tax, amounts.advanceDeduction,
                amounts.finalPayable
        );

        invoices.add(invoice);
        return invoice;
    }

    // Recalculates the existing invoice after modifying a reservation.
    public boolean updateInvoiceForBooking(String bookingId) {

        if (!K2636521InputValidator.isNotBlank(bookingId)) {
            return false;
        }

        K2636521Invoice invoice = findInvoiceByBookingId(bookingId);

        if (invoice == null
                || invoice.getInvoiceStatus()
                        != K2636521InvoiceStatus.ACTIVE) {
            return false;
        }

        K2636521Reservation reservation = invoice.getReservation();

        if (reservation.getStatus()
                        != K2636521BookingStatus.CONFIRMED
                || !reservation.isAdvancePaid()
                || reservation.isAdvanceRefunded()
                || reservation.getNumberOfNights() <= 0
                || reservation.getNumberOfGuests() <= 0) {
            return false;
        }

        InvoiceAmounts amounts = calculateInvoiceAmounts(reservation);

        if (amounts == null) {
            return false;
        }

        invoice.updateAmounts(amounts.baseCharge, amounts.extraGuestCharge,
                amounts.discount, amounts.taxRate, amounts.tax,
                amounts.advanceDeduction, amounts.finalPayable
        );

        return true;
    }

    // Calculates invoice amounts using the reservation details.
    private InvoiceAmounts calculateInvoiceAmounts(K2636521Reservation reservation) {

        long nights = reservation.getNumberOfNights();

        if (nights <= 0 || reservation.getRoom() == null) {
            return null;
        }

        BigDecimal baseCharge = reservation.getRoom()
                .getDailyRate()
                .multiply(BigDecimal.valueOf(nights));

        BigDecimal taxRate = reservation.getRoom().getTaxRate();

        int extraGuests = Math.max(
                0,
                reservation.getNumberOfGuests()
                        - reservation.getRoom().getCapacity());

        BigDecimal extraGuestCharge = reservation.getRoom()
                .getExtraGuestCharge()
                .multiply(BigDecimal.valueOf(extraGuests))
                .multiply(BigDecimal.valueOf(nights));

        BigDecimal discount = BigDecimal.ZERO;

        if (nights >= 5) {
            discount = baseCharge.multiply(DISCOUNT_RATE);
        }

        BigDecimal discountedRoomCharge = baseCharge.subtract(discount);

        BigDecimal taxableAmount = discountedRoomCharge.add(extraGuestCharge);

        BigDecimal tax = taxableAmount
                .multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal advanceDeduction = BigDecimal.ZERO;

        if (reservation.isAdvancePaid()
                && !reservation.isAdvanceRefunded()) {
            advanceDeduction = BOOKING_ADVANCE;
        }

        BigDecimal finalPayable = taxableAmount
                .add(tax)
                .subtract(advanceDeduction)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new InvoiceAmounts(baseCharge.setScale(2, RoundingMode.HALF_UP),
                extraGuestCharge.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP),
                taxRate,
                tax,
                advanceDeduction,
                finalPayable
        );
    }

    // Stores calculated invoice amounts.
    private static class InvoiceAmounts {

        private BigDecimal baseCharge;
        private BigDecimal extraGuestCharge;
        private BigDecimal discount;
        private BigDecimal taxRate;
        private BigDecimal tax;
        private BigDecimal advanceDeduction;
        private BigDecimal finalPayable;

        private InvoiceAmounts(
                BigDecimal baseCharge,
                BigDecimal extraGuestCharge,
                BigDecimal discount,
                BigDecimal taxRate,
                BigDecimal tax,
                BigDecimal advanceDeduction,
                BigDecimal finalPayable) {

            this.baseCharge = baseCharge;
            this.extraGuestCharge = extraGuestCharge;
            this.discount = discount;
            this.taxRate = taxRate;
            this.tax = tax;
            this.advanceDeduction = advanceDeduction;
            this.finalPayable = finalPayable;
        }
    }

    // Marks the existing invoice as voided after cancellation.
    public boolean voidInvoiceForBooking(
            String bookingId, String cancelledBy) {

        if (!K2636521InputValidator.isNotBlank(bookingId)
                || !K2636521InputValidator.isNotBlank(cancelledBy)) {
            return false;
        }

        K2636521Invoice invoice =
                findInvoiceByBookingId(bookingId);

        if (invoice == null
                || invoice.getInvoiceStatus()
                        != K2636521InvoiceStatus.ACTIVE
                || invoice.getReservation().getStatus()
                        != K2636521BookingStatus.CANCELLED) {
            return false;
        }

        BigDecimal refundAmount = BigDecimal.ZERO;

        if (invoice.getReservation().isAdvanceRefunded()) {
            refundAmount = BOOKING_ADVANCE;
        }

        invoice.voidInvoice(
                cancelledBy.trim().toUpperCase(),
                refundAmount);

        return true;
    }

    // Generates the next unique invoice ID.
    private String generateInvoiceId() {
        int highestId = 0;

        for (K2636521Invoice invoice : invoices) {
            String id = invoice.getInvoiceId();

            if (id != null && id.matches("INV\\d+")) {
                int number = Integer.parseInt(id.substring(3));
                highestId = Math.max(highestId, number);
            }
        }

        return String.format("INV%03d", highestId + 1);
    }

    public K2636521Invoice findInvoiceById(String invoiceId) {
        if (!K2636521InputValidator.isNotBlank(invoiceId)) {
            return null;
        }

        for (K2636521Invoice invoice : invoices) {
            if (invoice.getInvoiceId()
                    .equalsIgnoreCase(invoiceId.trim())) {
                return invoice;
            }
        }

        return null;
    }

    public K2636521Invoice findInvoiceByBookingId(String bookingId) {
        if (!K2636521InputValidator.isNotBlank(bookingId)) {
            return null;
        }

        for (K2636521Invoice invoice : invoices) {
            if (invoice.getReservation().getBookingId()
                    .equalsIgnoreCase(bookingId.trim())) {
                return invoice;
            }
        }

        return null;
    }

    public List<K2636521Invoice> getAllInvoices() {
        return new ArrayList<>(invoices);
    }
}