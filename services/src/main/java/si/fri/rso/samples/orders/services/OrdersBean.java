package si.fri.rso.samples.orders.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Timed;
import si.fri.rso.samples.orders.entities.Feedback;
import si.fri.rso.samples.orders.entities.Order;
import si.fri.rso.samples.orders.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class OrdersBean {

    @Inject
    private AppProperties appProperties;

    private Client httpClient;
    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        // baseUrl = "http://159.122.187.177:31465"; // only for demonstration
    }

    /*@Inject
    @DiscoverService("feedback")
    private Optional<String> baseUrl;*/

    @Inject
    private OrdersBean ordersBean;

    private Logger log = Logger.getLogger(OrdersBean.class.getName());

    @Inject
    private EntityManager em;

    public List<Order> getOrders(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, Order.class, queryParameters);

    }
    public List<Order> getOrders() {

        TypedQuery<Order> query = em.createNamedQuery("Order.getAll", Order.class);

        return query.getResultList();

    }

    public Order getOrder(Integer orderId) {

        Order order = em.find(Order.class, orderId);

        if (order == null) {
            throw new NotFoundException();
        }

     //   List<Feedback> feedbacks = ordersBean.getFeedbacks(orderId);
       // order.setFeedbacks(feedbacks);
        return order;
    }

    public Order createOrder(Order order) {

        try {
            beginTx();
            em.persist(order);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return order;
    }

    public Order putOrder(Integer orderId, Order order) {

        Order c = em.find(Order.class, orderId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            order.setId(c.getId());
            order = em.merge(order);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return order;
    }

    public Order completeOrder(Integer orderId) {

        Order order = em.find(Order.class, orderId);

        if (order == null) {
            throw new NotFoundException();
        }

        try {
            beginTx();


            order.setTimeTo(Instant.now());
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return order;
    }

    public boolean deleteOrder(String orderId) {

        Order order = em.find(Order.class, orderId);

        if (order != null) {
            try {
                beginTx();
                em.remove(order);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }



    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }

    @Timed
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getFeedbackFallback")

    private static String getJSONResponse(String requestType, String fullUrl) {
        return getJSONResponse( requestType,  fullUrl, null);
    }

    private static String getJSONResponse(String requestType, String fullUrl, String json) {
        try {

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = null;

            if ("GET".equals(requestType)) {
                HttpGet request = new HttpGet(fullUrl);
                response = httpClient.execute(request);

            } else if ("POST".equals(requestType)) {
                HttpPost request = new HttpPost(fullUrl);

                request.setEntity(new StringEntity(json));
                request.setHeader("Content-type", "application/json");
                request.setHeader("Accept", "application/json");

                response = httpClient.execute(request);

            } else {
                throw new InternalServerErrorException("Wrong request type:" + requestType);
            }

            int status = response.getStatusLine().getStatusCode();
            System.out.println("response code: " + status);
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null)
                    return EntityUtils.toString(entity);
            } else {
                String msg = "Remote server '" + fullUrl + "' is responded with status " + status + ".";
                System.out.println(msg);
                // todo logging
                throw new InternalServerErrorException(msg);
            }

        } catch (IOException e) {
            String msg = e.getClass().getName() + " occured: " + e.getMessage();
            // todo logging
            System.out.println(msg);
            throw new InternalServerErrorException(msg);
        }
        return "{}"; //empty json
    }
}
