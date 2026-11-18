package smartstay.model;

import java.math.BigDecimal;

/* Stores the invoice details for a reservation. */
public class K2636521Invoice {

    private String invoiceId;
    private K2636521Reservation reservation;
    private BigDecimal baseCharge;
    private BigDecimal extraGuestCharge;
    private BigDecimal discount;
    private BigDecimal taxRate;
    private BigDecimal tax;
    private BigDecimal advanceDeduction;
    private BigDecimal finalPayable;
    private K2636521InvoiceStatus invoiceStatus;
    private String cancelledBy;
    private BigDecimal refundAmount;

    public K2636521Invoice( String invoiceId, K2636521Reservation reservation,
            BigDecimal baseCharge, BigDecimal extraGuestCharge,
            BigDecimal discount, BigDecimal taxRate,
            BigDecimal tax, BigDecimal advanceDeduction,
            BigDecimal finalPayable) {

        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.baseCharge = baseCharge;
        this.extraGuestCharge = extraGuestCharge;
        this.discount = discount;
        this.taxRate = taxRate;
        this.tax = tax;
        this.advanceDeduction = advanceDeduction;
        this.finalPayable = finalPayable;

        this.invoiceStatus = K2636521InvoiceStatus.ACTIVE;
        this.cancelledBy = null;
        this.refundAmount = BigDecimal.ZERO;
    }

    // Returns the invoice ID.
    public String getInvoiceId() {
        return invoiceId;
    }

    // Returns the reservation associated with the invoice.
    public K2636521Reservation getReservation() {
        return reservation;
    }

    // Returns the base charge for the reservation.
    public BigDecimal getBaseCharge() {
        return baseCharge;
    }

    // Returns the extra guest charge for the reservation.
    public BigDecimal getExtraGuestCharge() {
        return extraGuestCharge;
    }

    // Returns the discount applied to the reservation.
    public BigDecimal getDiscount() {
        return discount;
    }

    // Returns the tax rate applied to the reservation.
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    // Returns the tax amount for the reservation.
    public BigDecimal getTax() {
        return tax;
    }

    // Returns the advance deduction for the reservation.
    public BigDecimal getAdvanceDeduction() {
        return advanceDeduction;
    }

    // Returns the final payable amount for the reservation.
    public BigDecimal getFinalPayable() {
        return finalPayable;
    }

    // Returns the status of the invoice.
    public K2636521InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    // Returns the user who cancelled the invoice.
    public String getCancelledBy() {
        return cancelledBy;
    }

    // Returns the amount to be refunded.
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    // Updates invoice amounts after modifying a reservation.
    public void updateAmounts(BigDecimal baseCharge, BigDecimal extraGuestCharge,
            BigDecimal discount, BigDecimal taxRate,
            BigDecimal tax, BigDecimal advanceDeduction,
            BigDecimal finalPayable) {

        this.baseCharge = baseCharge;
        this.extraGuestCharge = extraGuestCharge;
        this.discount = discount;
        this.taxRate = taxRate;
        this.tax = tax;
        this.advanceDeduction = advanceDeduction;
        this.finalPayable = finalPayable;
    }

    // Voids the invoice and records cancellation details.
    public void voidInvoice(String cancelledBy, BigDecimal refundAmount) {

        this.invoiceStatus = K2636521InvoiceStatus.VOIDED;
        this.cancelledBy = cancelledBy;
        this.refundAmount = refundAmount;
    }
}