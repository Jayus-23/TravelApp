package lewis.com.carpart.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import lewis.com.carpart.R;
import lewis.com.carpart.base.BaseActivity;
import lewis.com.carpart.bean.Img;
import lewis.com.carpart.utils.JsonParser;
import lewis.com.carpart.utils.SimpleAdapter;

/**
 * Created by Administrator on 2019/5/7.
 */

public class SearchAct extends BaseActivity {
    @BindView(R.id.et)
    EditText et;
    @BindView(R.id.tv_yy)
    TextView tv_yy;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;//上下滑不丢失
    private SimpleAdapter<Img> adapter;
    private List<Img> imgs=new ArrayList<>();
    private List<Img> allimgs=new ArrayList<>();
    @Override
    public int intiLayout() {
        return R.layout.act_search;
    }
    private void initSpeech() {
        // 将“12345678”替换成您申请的 APPID，申请地址： http://www.xfyun.cn
        // 请勿在 “ =”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5c2dbcc0");
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                toast("初始化失败 ");
            }

        }
    }

    private void startSpeechDialog() {
        MyInitListener myInitListener = new MyInitListener();
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this,myInitListener);
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new SearchAct.MyRecognizerDialogListener());


        //4. 显示dialog，接收语音输入
        mDialog.show();
    }
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //为解析的
           // toast(result);

            System.out.println(" 没有解析的 :" + result);

            String text = JsonParser.parseIatResult(result);//解析过后的
            System.out.println(" 解析后的 :" + text);

            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);//没有得到一句，添加到

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            String s = resultBuffer.toString();
            if (TextUtils.isEmpty(s)){
                toast("没听清你说的");
                return;
            }
//            imgs.clear();
//            for (Img img:allimgs
//                    ) {
//                if (img.address.contains(s)){
//                    imgs.add(img);
//                }
//            }
//            adapter.notifyDataSetChanged();
            et.setText(s);


        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }

    @Override
    public void initView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter = new SimpleAdapter<Img>(R.layout.item_img, imgs, new SimpleAdapter.ConVert<Img>() {
            @Override
            public void convert(BaseViewHolder helper, Img img) {
                helper.setText(R.id.tv_address, img.address);
                ImageView view = helper.getView(R.id.iv);
                Glide.with(SearchAct.this).load(img.imgFile.getUrl()).into(view);
            }
        });
        recyclerView.setAdapter(adapter);
        initSpeech();
        tv_yy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechDialog();
            }
        });
    }

    @Override
    public void initData() {
        getdata();
    }



    @OnClick(R.id.tv_s)
    public void onViewClicked() {
        String s = et.getText().toString();
        if (TextUtils.isEmpty(s)){
            toast("请输入地址");
            return;
        }
        imgs.clear();
        for (Img img:allimgs
             ) {
            if (img.address.contains(s)){
                imgs.add(img);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getdata(){
        BmobQuery<Img> bmobQuery=new BmobQuery<>();

        bmobQuery.findObjects(this, new FindListener<Img>() {
            @Override
            public void onSuccess(List<Img> list) {
                allimgs.addAll(list);
                imgs.addAll(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

}
