package com.timecat.module.timetable.app;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.jess.arms.base.delegate.AppLifecycles;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.integration.ConfigModule;
import java.util.List;
import org.litepal.LitePal;

/**
 * ================================================ CommonSDK 的 GlobalConfiguration
 * 含有有每个组件都可公用的配置信息, 每个组件的 AndroidManifest 都应该声明此 ConfigModule
 *
 * @see <a href="https://github.com/JessYanCoding/ArmsComponent/wiki#3.3">ConfigModule wiki 官方文档</a>
 * Created by JessYan on 30/03/2018 17:16
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class GlobalConfiguration implements ConfigModule {

  @Override
  public void applyOptions(Context context, GlobalConfigModule.Builder builder) {
  }

  @Override
  public void injectAppLifecycle(Context context, List<AppLifecycles> lifecycles) {
    // AppDelegate.Lifecycle 的所有方法都会在基类Application对应的生命周期中被调用,所以在对应的方法中可以扩展一些自己需要的逻辑

    lifecycles.add(new AppLifecycles() {

      @Override
      public void attachBaseContext(@NonNull Context base) {
      }

      @Override
      public void onCreate(@NonNull Application application) {
        //初始化所有的第三方
        LitePal.initialize(application);
      }

      @Override
      public void onTerminate(@NonNull Application application) {

      }
    });
  }

  @Override
  public void injectActivityLifecycle(Context context,
      List<Application.ActivityLifecycleCallbacks> lifecycles) {
  }

  @Override
  public void injectFragmentLifecycle(Context context,
      List<FragmentManager.FragmentLifecycleCallbacks> lifecycles) {
  }

}
