package kiyv.domain.dbf;

import kiyv.domain.model.Order;

import java.util.Map;

public interface OrderDbf {

    Map<String, Order> getAll();

}
