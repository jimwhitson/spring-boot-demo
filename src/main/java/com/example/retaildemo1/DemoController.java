package com.example.retaildemo1;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;

@RestController
public class DemoController {
    static private Cluster cluster;
    static private Bucket bucket;

    static {
        cluster = CouchbaseCluster.create("couchbase://34.255.147.194");
        cluster.authenticate("Administrator", "password");
        bucket = cluster.openBucket("pricing3");
    }

    @RequestMapping("/latestAtStore/{product_id}/{store_id}")
    public String storeProductLookup(@PathVariable Integer product_id, @PathVariable Integer store_id) {
        N1qlQueryResult result = bucket.query(
                N1qlQuery.parameterized(
                        "select store_pricing.store_id, " +
                        "store_pricing.product_id, " +
                        "store_pricing.store_group_id, " +
                        "store_pricing.store_prices[0] as latest_store_price " +
                        "FROM pricing3 as store_pricing " +
                        "WHERE store_pricing.type=\"store_product\" " +
                        "AND store_pricing.product_id = $1 " +
                        "AND store_pricing.store_id = $2;", JsonArray.from(product_id, store_id))
        );
        return result.toString();
    }

    @RequestMapping("/latestAtStoreWithGroup/{product_id}/{store_id}")
    public String storeAndGroupLatestPrice(@PathVariable Integer product_id, @PathVariable Integer store_id) {
        N1qlQueryResult result = bucket.query(
                N1qlQuery.parameterized("SELECT store_pricing.store_id, store_pricing.product_id, store_pricing.store_group_id,\n" +
                                "       store_pricing.store_prices[0] as latest_store_prices,\n" +
                                "       group_pricing.store_group_prices[0] as latest_group_prices\n" +
                                "\n" +
                                "FROM pricing3 AS store_pricing\n" +
                                " JOIN pricing3 AS group_pricing\n" +
                                " ON store_pricing.store_group_id = group_pricing.store_group_id\n" +
                                "\n" +
                                "   WHERE store_pricing.type = \"store_product\"\n" +
                                "   AND group_pricing.type=\"store_group_product\"\n" +
                                "   AND store_pricing.store_id = $1\n" +
                                "   AND group_pricing.product_id = $2\n" +
                                "   AND store_pricing.product_id = $2",
                        JsonArray.from(store_id, product_id)));
        return result.toString();
    }
}
