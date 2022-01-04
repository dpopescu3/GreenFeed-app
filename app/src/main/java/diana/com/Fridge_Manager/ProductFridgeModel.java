package diana.com.Fridge_Manager;

public class ProductFridgeModel {
    public String product;
    public String expdate;
    public String weight;
    public String qunatity;
    public String brand;
    public String origin;

    public ProductFridgeModel() {
    }

    public ProductFridgeModel(String product, String expdate, String weight, String qunatity, String brand, String origin) {
        this.product = product;
        this.expdate = expdate;
        this.weight = weight;
        this.qunatity = qunatity;
        this.brand = brand;
        this.origin = origin;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getExpdate() {
        return expdate;
    }

    public void setExpdate(String expdate) {
        this.expdate = expdate;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getQunatity() {
        return qunatity;
    }

    public void setQunatity(String qunatity) {
        this.qunatity = qunatity;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
