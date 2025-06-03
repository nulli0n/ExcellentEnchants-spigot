package su.nightexpress.excellentenchants.api.damage;

import org.jetbrains.annotations.NotNull;

public class DamageBonus {

    private final DamageBonusType type;

    private double bonusAmount;

    public DamageBonus(@NotNull DamageBonusType type) {
        this.type = type;
    }

    public void addBonus(double amount, double capacity) {
        if (this.bonusAmount + amount >= capacity) {
            this.setBonusAmount(capacity);
            return;
        }

        this.addBonus(amount);
    }

    public void addBonus(double amount) {
        this.setBonusAmount(this.bonusAmount + Math.abs(amount));
    }

    public void addPenalty(double amount, double capacity) {
        if (Math.abs(this.bonusAmount) + Math.abs(amount) >= Math.abs(capacity)) {
            this.setBonusAmount(-Math.abs(capacity));
            return;
        }

        this.addPenalty(amount);
    }

    public void addPenalty(double amount) {
        this.setBonusAmount(this.bonusAmount - Math.abs(amount));
    }

    @NotNull
    public DamageBonusType getType() {
        return this.type;
    }

    public double getBonusAmount() {
        return this.bonusAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }
}
