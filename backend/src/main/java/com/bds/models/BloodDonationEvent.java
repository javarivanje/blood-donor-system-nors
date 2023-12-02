package com.bds.models;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "blood_donation_event")
@Table(
        name = "blood_donation_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "blood_donation_event_unique",
                        columnNames = {"event_name", "event_date", "blood_type"}
                )
        }
)
public class BloodDonationEvent {

    @Id
    @SequenceGenerator(
            name = "blood_donation_event_id_seq",
            sequenceName = "blood_donation_event_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "blood_donation_event_id_seq"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long Id;

    @Column(
            name = "event_name",
            nullable = false
    )
    private String eventName;

    @Column(
            name = "event_date",
            nullable = false
    )
    private LocalDate eventDate;

    @Column(
            name = "blood_type",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private BloodType blood_type;

    @Column(
            name = "units",
            nullable = false
    )
    private Integer units;

    @ManyToOne(/*fetch = FetchType.LAZY*/)
    @JoinColumn(
            name = "organizer_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_organizer_id"
            )
    )
    private Users users;

    public BloodDonationEvent(Long id, String eventName, LocalDate eventDate, BloodType blood_type, Users users) {
        this.Id = id;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.blood_type = blood_type;
        this.users = users;
    }

    public BloodDonationEvent(String eventName, LocalDate eventDate, BloodType blood_type, Integer units, Users users) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.blood_type = blood_type;
        this.units = units;
        this.users = users;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public BloodDonationEvent() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public BloodType getBlood_type() {
        return blood_type;
    }

    public void setBlood_type(BloodType blood_type) {
        this.blood_type = blood_type;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodDonationEvent that = (BloodDonationEvent) o;
        return Objects.equals(Id, that.Id) && Objects.equals(eventName, that.eventName) && Objects.equals(eventDate, that.eventDate) && blood_type == that.blood_type && Objects.equals(units, that.units) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, eventName, eventDate, blood_type, units, users);
    }

    @Override
    public String toString() {
        return "BloodDonationEvent{" +
                "Id=" + Id +
                ", eventName='" + eventName + '\'' +
                ", eventDate=" + eventDate +
                ", blood_type=" + blood_type +
                ", units=" + units +
                ", users=" + users +
                '}';
    }
}
