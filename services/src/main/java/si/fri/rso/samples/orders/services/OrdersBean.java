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
import si.fri.rso.samples.orders.entities.Feedback;
import si.fri.rso.samples.orders.entities.Order;
import si.fri.rso.samples.orders.services.configuration.AppProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class OrdersBean {

    @Inject
    private AppProperties appProperties;

    @Inject
    @DiscoverService("feedback")
    private Optional<String> baseUrl;

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

        List<Feedback> feedbacks = ordersBean.getFeedbacks(orderId);
        order.setFeedbacks(feedbacks);
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
    public List<Feedback> getFeedbacks(Integer orderId) {


        if (appProperties.isExternalServicesEnabled() && baseUrl.isPresent()) {
           /* try {
                String json = getJSONResponse("GET", baseUrl.get());
                ObjectMapper mapper = new ObjectMapper();

                Order driverId = mapper.readValue(json, Order.class);

                return httpClient
                        .target(baseUrl.get() + "/v1/orders?where=customerId:EQ:" + customerId)
                        .request().get(new GenericType<List<Order>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }*/
            try {
                System.out.println(" URL is " + baseUrl.get());

                String json = getJSONResponse("GET", baseUrl.get() + "/v1/feedback?where=orderId:EQ:" + orderId);
                ObjectMapper objectMapper = new ObjectMapper();

                List <Feedback> driverId = objectMapper.readValue(json,objectMapper.getTypeFactory().constructCollectionType(List.class, Feedback.class));
                for(Feedback feedbacks: driverId){

                }
                return driverId;

            } catch (IOException e) {
                System.out.println("Fail");
                return new ArrayList<>();
            }
        }



        return null;

    }

    public List<Feedback> getFeedbackFallback(Integer orderId) {

        return Collections.emptyList();

    }
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
