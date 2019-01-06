package si.fri.rso.samples.orders.entities;

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

    private Integer id1;
    private Instant time_from;

    private Instant time_to;

    private String pickup_location;

    private String drop_location;


    @Column(name = "customer_id")
    private String customerId;

    @ElementCollection
    private List<String> itemIds;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "car_id")
    private String carId;

    @ElementCollection
    private List<String> carIds;

    public Integer getCarId() {
        return id1;
    }

    public void setCarId(Integer id1) {
        this.id1 = id1;
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

    public List<String> getItemIds() {
        return itemIds;
    }

    public List<String> getCarIdsIds() {
        return carIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

    public void setCarIds(List<String> carIds) {
        this.carId = carId;
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
}
