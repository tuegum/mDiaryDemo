package com.tuegum.mDiaryDemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;


import com.gyf.barlibrary.ImmersionBar;
import com.tuegum.mDiaryDemo.Common.RecyclerItemClickListener;
import com.tuegum.mDiaryDemo.Common.diary;
import com.tuegum.mDiaryDemo.Common.diaryAdapter;
import com.tuegum.mDiaryDemo.EditActivity.EditActivity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {

    private Button mBuildbtn, mDeleteBtn;
    private RecyclerView mRv;
    public ImmersionBar mImmersionBar;

    @BindView(R.id.main_toolbar)
    Toolbar main_toolbar;


    //要删除的项的标志HashMap数组
    HashMap<Integer, String> str;
    //设置是否在onStart中更新数据源的标志
    private int update = 0;
    //存储diary对象的数组
    private List<diary> diaryList;
    //适配器
    private diaryAdapter adapter;
    //是否处于多选删除状态
    // 设置这个变量是为了让区分正常点击和多选删除时的点击事件
    //以及长按状态时不再响应长按事件
    private boolean isDeleteState;
    //网格布局管理器
    GridLayoutManager gridLayoutManager;
    //线性布局管理器
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(R.color.mediumaquamarine).init();

        bindView();
        initData();
        initView();

    }

    private void bindView() {
        mRv = findViewById(R.id.recycler_view);
        mBuildbtn = findViewById(R.id.build_button);
        mDeleteBtn = findViewById(R.id.delete_button);
    }

    private void initData() {
        str = new HashMap<>();
        diaryList = new ArrayList<>();
        isDeleteState = false;
        gridLayoutManager = new GridLayoutManager(this, 2);
        linearLayoutManager = new LinearLayoutManager(this);
    }

    private void initView() {

        //从数据库中获取日记
        diaryList = LitePal.findAll(diary.class);
        mRv = (RecyclerView) findViewById(R.id.recycler_view);
        if (diaryList.size() > 0) {
            //数据库中有日记记录，以网格布局展示
            mRv.setLayoutManager(gridLayoutManager);
        } else {
            //数据库中没有日记记录，用线性布局显示“无数据”
            mRv.setLayoutManager(linearLayoutManager);
        }
        //适配器的初始化，第一个参数传入数据源，第二个参数false表示正常状态;true表示多选删除状态
        adapter = new diaryAdapter(diaryList, false);
        mRv.setAdapter(adapter);
        //初始化‘新建’和‘删除’按钮
        mBuildbtn = (Button) findViewById(R.id.build_button);
        mDeleteBtn = (Button) findViewById(R.id.delete_button);
        //点击‘新建’按钮时，跳转到编辑日记的活动
        mBuildbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("diaryContent", "");//传入日记内容，这里为空
                intent.putExtra("signal", 0);//传入‘新建’标志：0
                startActivity(intent);
            }
        });

        //点击‘删除按钮时，执行删除操作
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelections();
            }
        });

        //为RecyclerView添加点击事件响应和长按事件响应
        mRv.addOnItemTouchListener(new RecyclerItemClickListener(this, mRv, new RecyclerItemClickListener.OnItemClickListener() {

            //点击事件响应
            @Override
            public void onItemClick(View view, int position) {

                if (!isDeleteState && diaryList.size() > 0) {
                    // 普通点击事件
                    diary mDiary = diaryList.get(position);
                    String diaryContent = mDiary.getContent().toString();
                    String diaryTime = mDiary.getTime().toString();
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    //传递内容
                    intent.putExtra("diaryContent", diaryContent);
                    //传入修改标志1：表示修改原有日记内容
                    intent.putExtra("signal", 1);
                    startActivity(intent);
                } else if (diaryList.size() > 0) {
                    //长按进入多选状态后的点击事件
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        str.remove(position);
                    } else {
                        str.put(position, diaryAdapter.mList.get(position).getContent());
                        checkBox.setChecked(true);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

                // 长按事件
                if (!isDeleteState && diaryList.size() > 0) {
                    isDeleteState = true;
                    str.clear();
                    //把当前选中的的复选框设置为选中状态
                    diaryAdapter.isSelected.put(position, true);
                    //把所有的CheckBox显示出来
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    //第二个参数为true表示长按进入多选删除状态时的适配器初始化
                    adapter = new diaryAdapter(diaryList, true);
                    recyclerView.setAdapter(adapter);
                    str.put(position, diaryAdapter.mList.get(position).getContent());
                }
            }

        }));

    }

    //执行删除的函数
    private void deleteSelections() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (str.size() == 0) {
            builder.setTitle("提示").setMessage("当前未选中项目").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    adapter = new diaryAdapter(diaryList, false);
                    recyclerView.setAdapter(adapter);
                    isDeleteState = false;
                    str.clear();
                }
            }).create().show();
        } else {
            builder.setTitle("提示");
            builder.setMessage("确认删除所选日记？");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i : str.keySet()) {
                        LitePal.deleteAll(diary.class, "content=?", str.get(i));
                    }
                    diaryList.clear();
                    List<diary> data = LitePal.findAll(diary.class);
                    for (diary mdiary : data) {
                        diaryList.add(mdiary);
                    }
                    adapter.notifyDataSetChanged();
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    if (diaryList.size() > 0) {
                        recyclerView.setLayoutManager(gridLayoutManager);
                    } else {
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                    adapter = new diaryAdapter(diaryList, false);
                    recyclerView.setAdapter(adapter);
                    isDeleteState = false;
                    str.clear();
                    Toast.makeText(MainActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    adapter = new diaryAdapter(diaryList, false);
                    recyclerView.setAdapter(adapter);
                    isDeleteState = false;
                    str.clear();
                }
            });
            builder.create().show();
        }
    }

    //用户返回该活动时
    @Override
    protected void onStart() {
        super.onStart();
        isDeleteState = false;
        if (update == 1) {
            diaryList.clear();
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            List<diary> data = LitePal.findAll(diary.class);
            for (diary mdiary : data) {
                diaryList.add(mdiary);
            }
            if (diaryList.size() > 0) {
                recyclerView.setLayoutManager(gridLayoutManager);
            } else {
                recyclerView.setLayoutManager(linearLayoutManager);
            }
            adapter = new diaryAdapter(diaryList, false);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        update = 1;//更新数据源
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImmersionBar.destroy();
    }

    /**
     * 设置标题栏
     */
    private void setToolbar() {
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//设计隐藏标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置显示返回键
        main_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish();
                Toast.makeText(MainActivity.this, "别乱点我啊!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

