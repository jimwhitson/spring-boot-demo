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
    static public Cluster cluster;
    static public Bucket bucket;

    static {
        cluster = CouchbaseCluster.create("couchbase://34.255.147.194");
        cluster.authenticate("Administrator", "password");
        bucket = cluster.openBucket("pricing3");
    }

    @RequestMapping("/store/{store_id}/product/{product_id}")
    public String storeProductLookup(@PathVariable String product_id, @PathVariable String store_id) {
        JsonDocument result = bucket.get(String.format("store::%s::product::%s", store_id, product_id));
        return result.toString();
    }

    @RequestMapping("/product/{product_id}")
    public String storeProductLookup(@PathVariable String product_id) {
        N1qlQueryResult result = bucket.query(
                N1qlQuery.parameterized("select * from pricing3 where foo = $1", JsonArray.from(product_id))
        );
        return result.toString();

    }
}
