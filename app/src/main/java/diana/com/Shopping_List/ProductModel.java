package diana.com.Shopping_List;

public class ProductModel {

    public String productName;
    public String productTime;
    public String productWeight;
    public String productQuantity;

    public ProductModel() {
    }

    public ProductModel(String productName, String productTime, String producWeight, String productQuantity) {
        this.productName = productName;
        this.productTime = productTime;
        this.productWeight = producWeight;
        this.productQuantity = productQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductTime() {
        return productTime;
    }

    public void setProductTime(String productTime) {
        this.productTime = productTime;
    }

    public String getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(String producWeight) {
        this.productWeight = producWeight;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }
}
