package si.fri.rso.samples.orders.api.v1.graphql;

import com.kumuluz.ee.graphql.annotations.GraphQLClass;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import si.fri.rso.samples.orders.services.OrdersBean;
import si.fri.rso.samples.orders.entities.Order;

import javax.inject.Inject;

@GraphQLClass
public class OrderMutations {

    @Inject
    private OrdersBean ordersBean;

    @GraphQLMutation
    public Order createOrder(@GraphQLArgument(name = "order") Order order) {
        ordersBean.createOrder(order);
        return order;
    }

    @GraphQLMutation
    public DeleteResponse deleteOrder(@GraphQLArgument(name = "id") String id) {
        return new DeleteResponse(ordersBean.deleteOrder(id));
    }

}
