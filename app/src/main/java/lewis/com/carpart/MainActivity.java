package lewis.com.carpart;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import lewis.com.carpart.base.BaseActivity;
import lewis.com.carpart.bean.Img;
import lewis.com.carpart.ui.AddAct;
import lewis.com.carpart.ui.LoginActivity;
import lewis.com.carpart.ui.SearchAct;


public class MainActivity extends BaseActivity {


    @BindView(R.id.mapview)
    MapView mapview;
    @BindView(R.id.tv_s)
    TextView tv_s;
    @BindView(R.id.add)
    TextView add;
    @BindView(R.id.exit)
    TextView exit;
    private BaiduMap mBaiduMap;
    private boolean isFirstLocation = true;
    private LocationClient mLocationClient;
    private String address;
    private String lat;
    private String log;
    @Override
    public int intiLayout() {
        return R.layout.activity_main;
    }


    public void initView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
        mapview = (MapView) findViewById(R.id.mapview);//获取地图控件
        mBaiduMap = mapview.getMap();//获取百度地图


        mBaiduMap.setMyLocationEnabled(true);//显示当前位置的图标

        initLoaction();//定位
        tv_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpAct(SearchAct.class);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpAct(LoginActivity.class);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(address)){
                    toast("请检查定位");
                    return;
                }
                Bundle bundle=new Bundle();
                bundle.putString("address",address);
                bundle.putString("log",log);
                bundle.putString("lat",lat);
                jumpAct(AddAct.class,bundle);//带参数跳转
            }
        });
    }

    @Override
    public void initData() {
//
//        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
//            //marker被点击时回调的方法
//            //若响应点击事件，返回true，否则返回false
//            //默认返回false
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                Bundle extraInfo = marker.getExtraInfo();
//                if (extraInfo!=null){
//                    Intent intent = new Intent(MainActivity.this, CarPartAct.class);
//                    intent.putExtras(extraInfo);
//                    startActivity(intent);
//                }
//                return true;
//            }
//        });
    }


    private void initLoaction() {
        mLocationClient = new LocationClient(this);

//通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(10000);
        option.setAddrType("all");
//设置locationClientOption
        mLocationClient.setLocOption(option);

//注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
//开启地图定位图层
        mLocationClient.start();
    }



    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mapview == null) {
                return;
            }

            if (isFirstLocation) {
                isFirstLocation = false;
                //设置并显示中心点
                setPosition2Center(mBaiduMap, location, true);
            }

        }
    }

    public void setPosition2Center(BaiduMap map, BDLocation bdLocation, Boolean isShowLoc) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(bdLocation.getRadius()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        map.setMyLocationData(locData);

        if (isShowLoc) {
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

            address = bdLocation.getAddress().address;
            lat = bdLocation.getLatitude()+"";
            log = bdLocation.getLongitude()+"";
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapview.onResume();
        getData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapview.onDestroy();
    }


    private void getData() {
        mBaiduMap.clear();

        BmobQuery<Img> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(this, new FindListener<Img>() {
            @Override
            public void onSuccess(List<Img> list) {
                if (list.size() > 0) {
                    for (final Img img : list
                            ) {
                        final BitmapDescriptor[] pic = {null};
                        //判断头像地址是否为空
                        if (!TextUtils.isEmpty(img.imgFile.getUrl())) {
                            //不为空就将地址传递过去加载到布局中
                            returnPictureView(img.imgFile.getUrl(), new ResultListener() {
                                @Override
                                public void onReturnResult(View view) {
                                    pic[0] = BitmapDescriptorFactory.fromView(view);
                                    putDataToMarkerOptions(pic[0], img);
                                }
                            });
                        } else {
                            //头像地址为空就加载本地图片
                            pic[0] = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_foreground);
                            putDataToMarkerOptions(pic[0], img);
                        }
                    }
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    //将图片加载到布局中
    private void returnPictureView(String imagUrl, final ResultListener resultListener) {
        final View markerView = LayoutInflater.from(this).inflate(R.layout.marker, null);
        final ImageView friendTouxiang = (ImageView) markerView.findViewById(R.id.iv);

        Glide.with(this)
                .load(imagUrl)

                .asBitmap()
                .error(R.drawable.ic_drag_to_delete)
                .override(150,150)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        friendTouxiang.setImageBitmap(resource);
                        resultListener.onReturnResult(markerView);
                    }


                });
    }
//

    //回调接口
    private interface ResultListener {
        void onReturnResult(View view);
    }

    //在地图上进行标记
    private void putDataToMarkerOptions(BitmapDescriptor pic, Img img) {
        LatLng point = new LatLng(Double.parseDouble(img.lat), Double.parseDouble(img.log));
        MarkerOptions overlayOptions = new MarkerOptions()
                .position(point)

                .title(img.address)
                .icon(pic)
                .zIndex(5);//设置marker从地上生长出来的动画

        Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("bean", img);
//        marker.setExtraInfo(bundle);//marker点击事件监听时，可以获取到此时设置的数据
//        marker.setToTop();
    }


}
