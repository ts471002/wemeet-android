package com.example.wemeet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.wemeet.pojo.InputTips;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class POISearchActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {
    private PoiSearch.Query query;// Poi查询条件类
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);
        ImageButton goBack = findViewById(R.id.back);
        goBack.setOnClickListener(this::onClick);
        listView = findViewById(R.id.inputtip_list);
        initSearchView();
    }

    private void initSearchView() {
        // 输入搜索关键字
        SearchView mSearchView = (SearchView) findViewById(R.id.keyWord);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                Toast.makeText(POISearchActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s!=null&&s.trim().length()!=0){
                    doSearch(s);
                } else {
                    listView.setAdapter(null);
                }
                return false;
            }
        });
        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
    }


    private void onClick(View view) {
        POISearchActivity.this.finish();
    }//点击返回按钮

    protected void doSearch(String key) {
        // 当前页面，从0开始计数
        int currentPage = 0;
        //不输入城市名称有些地方搜索不到
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(key, "", "北京");
        // 设置每页最多返回多少条poiItem
        query.setPageSize(10);
        // 设置查询页码
        query.setPageNum(currentPage);

        //构造 PoiSearch 对象，并设置监听
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        //调用 PoiSearch 的 searchPOIAsyn() 方法发送请求。
        poiSearch.searchPOIAsyn();

    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        //rCode 为1000 时成功,其他为失败
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            // Log.i("TAG-----------------", "成功了");
            // 解析result   获取搜索poi的结果
            if (result != null && result.getQuery() != null) {
                if (result.getQuery().equals(query)) {  // 是否是同一条
                    // poi返回的结果
                    ArrayList<InputTips> data = new ArrayList<>();//自己创建的数据集合
                    // 取得第一页的poiItem数据，页数从数字0开始
                    //poiResult.getPois()可以获取到PoiItem列表
                    List<PoiItem> poiItems = result.getPois();
                    //若当前城市查询不到所需POI信息，可以通过result.getSearchSuggestionCitys()获取当前Poi搜索的建议城市
                    //如果搜索关键字明显为误输入，则可通过result.getSearchSuggestionKeywords()方法得到搜索关键词建议。

                    //解析获取到的PoiItem列表
                    for (PoiItem item : poiItems) {
                        //获取经纬度对象
                        LatLonPoint llp = item.getLatLonPoint();
                        double lon = llp.getLongitude();
                        double lat = llp.getLatitude();
                        //返回POI的名称
                        String name = item.getTitle();
                        //返回POI的地址
                        String address = item.getSnippet();
                        data.add(new InputTips(name, address, lon, lat));
                    }

                    InputTipsAdapter inputTipsAdapter = new InputTipsAdapter(this, data);
                    //将数据和布局 显示到列表
                    listView.setAdapter(inputTipsAdapter);
                    listView.setOnItemClickListener((adapterView, view, position, id) -> {
                        InputTips selectedInputTips = data.get(position);
                        Toast.makeText(POISearchActivity.this,selectedInputTips.mName,Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Toast.makeText(POISearchActivity.this, "无搜索结果", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(POISearchActivity.this, "错误码" + rCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

}
