/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.core;

/**
 * A sample bean that we can construct via Jelly tags
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class Order {

    private Product product;
    private int amount;
    private double price;

    public Order() {
    }

    public String toString() {
        return "Order[amount=" + amount + ";price=" + price + ";product=" + product + "]";
    }

    /**
     * Factory method to create a new Product
     */
    public Product createProduct() {
        return new Product();
    }

    /**
     * Returns the amount.
     * @return int
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns the price.
     * @return double
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the amount.
     * @param amount The amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Sets the price.
     * @param price The price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns the product.
     * @return Product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product.
     * @param product The product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

}
