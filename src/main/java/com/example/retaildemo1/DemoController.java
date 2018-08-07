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

    @RequestMapping("/latest/{product_id}/{store_id}")
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
}
