package si.fri.rso.samples.orders.api.v1.graphql;

import com.kumuluz.ee.graphql.annotations.GraphQLClass;
import com.kumuluz.ee.graphql.classes.Filter;
import com.kumuluz.ee.graphql.classes.Pagination;
import com.kumuluz.ee.graphql.classes.PaginationWrapper;
import com.kumuluz.ee.graphql.classes.Sort;
import com.kumuluz.ee.graphql.utils.GraphQLUtils;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import si.fri.rso.samples.orders.entities.Order;
import si.fri.rso.samples.orders.services.OrdersBean;

import javax.inject.Inject;

@GraphQLClass
public class OrderQueries {

    @Inject
    private OrdersBean ordersBean;

    @GraphQLQuery
    public PaginationWrapper<Order> allOrders(@GraphQLArgument(name = "pagination") Pagination pagination,
                                              @GraphQLArgument(name = "sort") Sort sort,
                                              @GraphQLArgument(name = "filter") Filter filter) {
      return GraphQLUtils.process(ordersBean.getOrders(), pagination, sort, filter);
    }

    @GraphQLQuery
    public Order getCustomer(@GraphQLArgument(name = "id") Integer id) {
        return ordersBean.getOrder(id);
    }

}
