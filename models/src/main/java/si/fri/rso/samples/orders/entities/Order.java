package si.fri.rso.samples.orders.entities;

import si.fri.rso.samples.orders.entities.Feedback;
import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity(name = "orders")
@NamedQueries(value =
        {
                @NamedQuery(name = "Order.getAll", query = "SELECT o FROM orders o"),
                @NamedQuery(name = "Order.findByCustomer", query = "SELECT o FROM orders o WHERE o.customerId = " +
                        ":customerId"),
                @NamedQuery(name = "Order.findByCar", query = "SELECT o FROM orders o WHERE o.carId = " +
                        ":carId")
        })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Instant time_from;

    private Instant time_to;

    private String pickup_location;

    private String drop_location;


    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "car_id")
    private String carId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }


    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Instant getTimeFrom() {
        return time_from;
    }

    public void setTimeFrom(Instant time_from) {
        this.time_from = time_from;
    }

    public Instant getTimeTo() {
        return time_to;
    }

    public void setTimeTo(Instant time_to) {
        this.time_to = time_to;
    }

    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public String getPickup_location() {
        return pickup_location;
    }

    public void setDrop_location(String drop_location) {
        this.drop_location = drop_location;
    }

    public String getDrop_location() {
        return drop_location;
    }

    @Transient
    private List<Feedback> feedbacks;

    public List<Feedback> getFeedbacks() { return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) { this.feedbacks = feedbacks;
    }
}
