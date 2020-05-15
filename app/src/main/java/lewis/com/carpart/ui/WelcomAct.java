package lewis.com.carpart.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.view.WindowManager;

import lewis.com.carpart.MainActivity;
import lewis.com.carpart.R;
import lewis.com.carpart.base.BaseActivity;
import lewis.com.carpart.utils.ACache;


public class WelcomAct extends BaseActivity {



    @Override
    public int intiLayout() {
        return R.layout.act_wel;
    }

    @Override
    public void initView() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                String islogin = ACache.get(WelcomAct.this).getAsString("islogin");
                if (TextUtils.isEmpty(islogin)){
                    Intent intent = new Intent(WelcomAct.this,LoginActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(WelcomAct.this,MainActivity.class);
                    startActivity(intent);
               }

                finish();
            }
        },2000);
    }

    @Override
    public void initData() {

    }
}
