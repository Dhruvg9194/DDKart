package com.example.e_commerceapp.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_commerceapp.adapters.CategoryAdapter;
import com.example.e_commerceapp.adapters.ProductAdapter;
import com.example.e_commerceapp.databinding.ActivityMainBinding;
import com.example.e_commerceapp.models.Category;
import com.example.e_commerceapp.models.Product;
import com.example.e_commerceapp.utils.Constants;
import com.mancj.materialsearchbar.MaterialSearchBar;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    CategoryAdapter categoryAdapter;
    ArrayList<Category> categories;

    ProductAdapter productAdapter;
    ArrayList<Product> products;

    private List<String> lastsearches;
    private final int REQ_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());





        binding.searchBar.setSpeechMode(true);
//        binding.searchBar.isSpeechModeEnabled();
        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {

            @Override
            public void onSearchStateChanged(boolean enabled) {
                String s = enabled ? "enabled" : "disabled";
                Toast.makeText(MainActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("query", text.toString());
                startActivity(intent);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch(buttonCode) {
                    case MaterialSearchBar.BUTTON_SPEECH:
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                        try {
                            startActivityForResult(intent, REQ_CODE);
                        } catch (ActivityNotFoundException a) {
                            Toast.makeText(getApplicationContext(),
                                    "Sorry your device not supported",
                                    Toast.LENGTH_SHORT).show();
                        }
                }



            }
        });



        initCategories();
        initProducts();
        initSlider();
    }

    private void initSlider() {
        getRecentOffers();
    }

    void initCategories() {
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categories);

        getCategories();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        binding.CategoriesList.setLayoutManager(layoutManager);
        binding.CategoriesList.setAdapter(categoryAdapter);
    }

    void getCategories() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_CATEGORIES_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("err", response);
                    JSONObject mainObj = new JSONObject(response);
                    if(mainObj.getString("status").equals("success")) {
                        JSONArray categoriesArray = mainObj.getJSONArray("categories");
                        for(int i =0; i< categoriesArray.length(); i++) {
                            JSONObject object = categoriesArray.getJSONObject(i);
                            Category category = new Category(
                                    object.getString("name"),
                                    Constants.CATEGORIES_IMAGE_URL + object.getString("icon"),
                                    object.getString("color"),
                                    object.getString("brief"),
                                    object.getInt("id")
                            );
                            categories.add(category);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        // DO nothing
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(request);
    }

    void getRecentProducts() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.GET_PRODUCTS_URL + "?count=8";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")){
                    JSONArray productsArray = object.getJSONArray("products");
                    for(int i =0; i< productsArray.length(); i++) {
                        JSONObject childObj = productsArray.getJSONObject(i);
                        Product product = new Product(
                                childObj.getString("name"),
                                Constants.PRODUCTS_IMAGE_URL + childObj.getString("image"),
                                childObj.getString("status"),
                                childObj.getDouble("price"),
                                childObj.getDouble("price_discount"),
                                childObj.getInt("stock"),
                                childObj.getInt("id")

                        );
                        products.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> { });

        queue.add(request);
    }

    void getRecentOffers() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_OFFERS_URL, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")) {
                    JSONArray offerArray = object.getJSONArray("news_infos");
                    for(int i =0; i < offerArray.length(); i++) {
                        JSONObject childObj =  offerArray.getJSONObject(i);
                        binding.carousel.addData(
                                new CarouselItem(
                                        Constants.NEWS_IMAGE_URL + childObj.getString("image"),
                                        childObj.getString("title")
                                )
                        );
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {});
        queue.add(request);
    }

    void initProducts() {
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(this, products);

        getRecentProducts();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.ProductsList.setLayoutManager(layoutManager);
        binding.ProductsList.setAdapter(productAdapter);
    }
}