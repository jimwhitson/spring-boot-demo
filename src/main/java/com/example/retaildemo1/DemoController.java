package com.example.retaildemo1;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.x;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.i;




@RestController
public class DemoController {
    static private Cluster cluster;
    static private Bucket bucket;

    static {
        cluster = CouchbaseCluster.create("couchbase://<Server Address>");
        cluster.authenticate("Administrator", "password");
        bucket = cluster.openBucket("pricing");
    }

    @RequestMapping("/latestAtStore/{product_id}/{store_id}")
    public String storeProductLookup(@PathVariable Integer product_id, @PathVariable Integer store_id) {
        Statement stmt =
            select("store_pricing.store_id, "+
                        "store_pricing.product_id, " +
                        "store_pricing.store_group_id, " +
                        "store_pricing.store_prices[0] as latest_store_price")
                .from(i("pricing3")).as("store_pricing")
                .where(x("store_pricing.type").eq(s("store_product"))
                        .and(x("store_pricing.product_id").eq(x("$prod_id")))
                        .and(x("store_pricing.store_id").eq(x("$store_id")))
        );
        JsonObject venv = JsonObject.create()
                .put("prod_id", product_id)
                .put("store_id", store_id);
        N1qlQuery query = N1qlQuery.parameterized(stmt, venv);
        N1qlQueryResult result = bucket.query(query);
        return result.allRows().toString();
    }
}
