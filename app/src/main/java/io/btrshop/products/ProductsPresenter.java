package io.btrshop.products;

import android.support.annotation.NonNull;
import android.util.Log;

import io.btrshop.UseCase;
import io.btrshop.UseCaseHandler;
import io.btrshop.detailsproduct.domain.model.Product;
import io.btrshop.products.domain.usecase.GetProduct;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by charlie on 20/10/16.
 */
public class ProductsPresenter implements ProductsContract.Presenter {

    private ProductsContract.View mProductsView;
    private GetProduct mGetProduct;
    private UseCaseHandler mUseCaseHandler;

    public ProductsPresenter(@NonNull UseCaseHandler useCaseHandler,
                             @NonNull ProductsContract.View articlesView, @NonNull GetProduct getProduct) {
        mUseCaseHandler = checkNotNull(useCaseHandler, "usecaseHandler cannot be null");
        mProductsView = checkNotNull(articlesView, "productsView cannot be null!");
        mGetProduct = checkNotNull(getProduct, "getProduct cannot be null");
        mProductsView.setPresenter(this);


    }

    @Override
    public void scanProduct() {
        mProductsView.showScan();
    }

    @Override
    public void getProduct(String ean) {
        Log.d("PROD_PRES", "Get product : " + ean);

        checkNotNull(ean, "ean cannot be null!");
        mUseCaseHandler.execute(mGetProduct, new GetProduct.RequestValues(ean),
                new UseCase.UseCaseCallback<GetProduct.ResponseValue>() {

                    @Override
                    public void onSuccess(GetProduct.ResponseValue response) {
                        Product produitResponse = response.getProduct();
                        Log.d("PROD_PRES", "success");
                        if(produitResponse != null)
                            mProductsView.showProduct(response.getProduct());
                        else
                            mProductsView.showError();
                    }

                    @Override
                    public void onError() {
                        Log.d("EAN", "ERROR");
                    }
                });
    }

    @Override
    public void start() {

    }
}
