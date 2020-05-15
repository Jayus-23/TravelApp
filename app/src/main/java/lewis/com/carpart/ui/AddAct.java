package lewis.com.carpart.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.guoxiaoxing.phoenix.compress.picture.internal.PictureCompressor;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.ImageLoader;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.Phoenix;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import lewis.com.carpart.R;
import lewis.com.carpart.base.BaseActivity;
import lewis.com.carpart.bean.Img;

/**
 * Created by Administrator on 2019/5/7.
 */

public class AddAct extends BaseActivity {
    @BindView(R.id.iv)
    ImageView iv;
    private String compressPath;
    private String lat;
    private String log;
    private String address;

    @Override
    public int intiLayout() {
        return R.layout.act_add;
    }

    @Override
    public void initView() {
        Bundle extras = getIntent().getExtras();
        lat = extras.getString("lat");
        log = extras.getString("log");
        address = extras.getString("address");
        Phoenix.config()
                .imageLoader(new ImageLoader() {
                    @Override
                    public void loadImage(Context mContext, ImageView imageView
                            , String imagePath, int type) {
                        Glide.with(mContext)
                                .load(imagePath)
                                .into(imageView);
                    }
                });
    }

    @Override
    public void initData() {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<MediaEntity> result = Phoenix.result(data);
        MediaEntity entity = result.get(0);
        String localPath = entity.getLocalPath();
        Glide.with(AddAct.this).load(localPath).into(iv);
        File file = new File(localPath);
        try {
            File compressFIle = PictureCompressor.with(AddAct.this)
                    .savePath(AddAct.this.getCacheDir().getAbsolutePath())
                    .load(file)
                    .get();
            if (compressFIle != null) {
                compressPath = compressFIle.getAbsolutePath();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @OnClick({R.id.iv, R.id.tv_up})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv:
                Phoenix.with()
                        .theme(PhoenixOption.THEME_DEFAULT)// 主题
                        .fileType(MimeType.ofImage())//显示的文件类型图片、视频、图片和视频
                        .maxPickNumber(1)// 最大选择数量
                        .minPickNumber(0)// 最小选择数量
                        .spanCount(4)// 每行显示个数
                        .enablePreview(true)// 是否开启预览
                        .enableCamera(true)// 是否开启拍照
                        .enableAnimation(true)// 选择界面图片点击效果
                        .enableCompress(true)// 是否开启压缩
                        .compressPictureFilterSize(1024)//多少kb以下的图片不压缩
                        .compressVideoFilterSize(2018)//多少kb以下的视频不压缩
                        .thumbnailHeight(160)// 选择界面图片高度
                        .thumbnailWidth(160)// 选择界面图片宽度
                        .enableClickSound(false)// 是否开启点击声音

                        .mediaFilterSize(10000)//显示多少kb以下的图片/视频，默认为0，表示不限制
                        //如果是在Activity里使用就传Activity，如果是在Fragment里使用就传Fragment
                        .start(AddAct.this, PhoenixOption.TYPE_PICK_MEDIA, 100);
                break;
            case R.id.tv_up:
                if (TextUtils.isEmpty(compressPath)){
                    toast("请拍照");
                    return;
                }
                up();
                break;
        }
    }

    private void up() {
        final BmobFile bmobFile=new BmobFile(new File(compressPath));
        bmobFile.uploadblock(this, new UploadFileListener() {
            @Override
            public void onSuccess() {
                Img img=new Img();
                img.address=address;
                img.imgFile=bmobFile;
                img.lat=lat;
                img.log=log;
                img.account=userbean.account;
                img.save(AddAct.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        toast("发布成功");
                    }

                    @Override
                    public void onFailure(int i, String s) {

                    }
                });
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }
}
