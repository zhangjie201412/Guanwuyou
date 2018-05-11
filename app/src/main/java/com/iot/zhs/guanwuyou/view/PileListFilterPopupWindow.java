package com.iot.zhs.guanwuyou.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.iot.zhs.guanwuyou.MainActivity;
import com.iot.zhs.guanwuyou.NewTaskActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.comm.http.LoginUserModel;
import com.iot.zhs.guanwuyou.utils.Constant;
import com.iot.zhs.guanwuyou.utils.DisplayUtil;
import com.iot.zhs.guanwuyou.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by star on 2017/4/19.
 */

public class PileListFilterPopupWindow extends PopupWindow implements View.OnClickListener {
    private LinearLayout rootLayout;
    private TextView pileStateTv;//施工状态
    private EditText xEt;//x坐标
    private EditText yEt;//y坐标
    private TextView differGradeTv;//差异等级
    private TextView startDateTv;//开始时间
    private TextView endDateTv;//结束时间
    private Button sureBtn;//确定
    private Button resetBtn;//清除筛选

    private EditText searchEt;//搜索的输入框

    private Context context;
    private View rootView;
    private ImageView filterView;
    private OnFilterSureClickListener onFilterSureClickListener;
    private Map<String, Object> map = new HashMap<>();

    private String diffDregeeId = "";
    private String pileStateId = "";
    private String projectId = "";
    private String oldProjectId = "";//初始值
    private String oldProjectName = "";//初始值


    public Map<String, Object> paramsMap = new HashMap<>();
    public Map<String, Object> jsonStrMap = new HashMap<>();

    public int mYear ;
    public int mMonth ;
    public int mDay;
    public List<LoginUserModel.ConstructState> constructStateList=new ArrayList<>();
    public String[] constructStateStrArray;

    public PileListFilterPopupWindow(final Context context, final ImageView filterView, OnFilterSureClickListener onFilterSureClickListener) {
        this.context = context;
        this.filterView = filterView;
        this.onFilterSureClickListener = onFilterSureClickListener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.pop_pile_filter, null);
        initViews(contentView);
        this.setContentView(contentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        //this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        //popup的高度 = 屏幕高度-系统状态栏高度-app的标题栏高度
        int height = Constant.display.heightPixels - Constant.display.statusBarHeightPixels - 70;
        this.setHeight(height);

        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                filterView.setImageResource(R.mipmap.ic_advance_search);
                if (searchEt != null) {
                    searchEt.setEnabled(true);
                }
                dismiss();
            }
        });
        // 刷新状态
        this.update();
        getCurr();


    }

    private void getCurr(){
        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
    }

    public void setSearchEt(EditText searchEt) {
        this.searchEt = searchEt;
    }

  /*  public void setDifferGradeTv(String diffGrade) {
        differGradeTv.setText(diffGrade);
        for (ShowModel showModel : Constant.curUser.getDiffDegreeList()) {
            if (showModel.getShowName().equals(diffGrade)) {
                diffDregeeId = showModel.getId();
                break;
            }
        }
    }*/


    public void setPileStateTv(String index){
        String name=Constant.curUser.constructStateList.get(Integer.parseInt(index)).showName;
        pileStateId=Constant.curUser.constructStateList.get(Integer.parseInt(index)).id;
        pileStateTv.setText(name);
    }




    private void initViews(View contentView) {
        rootLayout = contentView.findViewById(R.id.root_layout);
        sureBtn = contentView.findViewById(R.id.sure_btn);
        resetBtn = contentView.findViewById(R.id.reset_btn);
        pileStateTv = contentView.findViewById(R.id.pile_state_tv);
        differGradeTv = contentView.findViewById(R.id.differ_grade_tv);
        startDateTv = contentView.findViewById(R.id.check_complete_start_date_tv);
        endDateTv = contentView.findViewById(R.id.check_complete_end_date_tv);
        xEt = contentView.findViewById(R.id.x_et);
        yEt = contentView.findViewById(R.id.y_et);


        rootLayout.setOnClickListener(this);
        sureBtn.setOnClickListener(this);
        pileStateTv.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        differGradeTv.setOnClickListener(this);
        startDateTv.setOnClickListener(this);
        endDateTv.setOnClickListener(this);


        constructStateList= Utils.deepCopy(Constant.curUser.constructStateList);
        constructStateList.add(0,new LoginUserModel.ConstructState("","全部"));
        constructStateStrArray=new String[constructStateList.size()];
        for(int i=0;i<constructStateList.size();i++){
            LoginUserModel.ConstructState constructState =constructStateList.get(i);
            constructStateStrArray[i]=constructState.showName;
        }
    }


    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            // this.showAtLocation(parent, Gravity.CENTER,0,0);
            filterView.setImageResource(R.mipmap.icon_filter_yello);
            this.showAsDropDown(parent);
            if (searchEt != null) {
                searchEt.setEnabled(false);
            }
        } else {
            filterView.setImageResource(R.mipmap.ic_advance_search);
            this.dismiss();
            if (searchEt != null) {
                searchEt.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.root_layout:

                filterView.setImageResource(R.mipmap.ic_advance_search);
                this.dismiss();
                if (searchEt != null) {
                    searchEt.setEnabled(true);
                }
                break;
            case R.id.sure_btn:
                map.put("constructionState", pileStateId);
                map.put("coordinatex", xEt.getText().toString().trim());
                map.put("coordinatey", yEt.getText().toString().trim());
                map.put("fillEndTimeStart", startDateTv.getText().toString().trim());//灌注结束的开始条件
                map.put("fillEndTimeEnd", endDateTv.getText().toString().trim());//灌注结束的结束条件
              //  map.put("diffGrade", diffDregeeId);//差异等级

                if (onFilterSureClickListener != null) {
                    onFilterSureClickListener.onFilterSureClick(map);
                }
                filterView.setImageResource(R.mipmap.ic_advance_search);
                this.dismiss();
                if (searchEt != null) {
                    searchEt.setEnabled(true);
                }
                break;
            case R.id.reset_btn:
                map.clear();
                pileStateTv.setText("");
                xEt.setText("");
                yEt.setText("");
                differGradeTv.setText("");
                startDateTv.setText("");
                endDateTv.setText("");

                diffDregeeId = "";
                pileStateId = "";
                projectId=oldProjectId;
                break;
            case R.id.pile_state_tv:
                new AlertDialog.Builder(context).setTitle("请选择").setItems(constructStateStrArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pileStateId=constructStateList.get(which).id;
                        pileStateTv.setText(constructStateList.get(which).showName);

                    }
                }).show();

                break;
            case R.id.differ_grade_tv:
                break;
            case R.id.check_complete_start_date_tv:
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        startDateTv.setText(formatDateStr(year,monthOfYear,dayOfMonth));
                    }
                },mYear,mMonth,mDay).show();
                break;
            case R.id.check_complete_end_date_tv:
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        endDateTv.setText(formatDateStr(year,monthOfYear,dayOfMonth));
                    }
                },mYear,mMonth,mDay).show();
                break;

        }
    }


    public interface OnFilterSureClickListener {
        void onFilterSureClick(Map<String, Object> map);
    }

    private String formatDateStr(int year, int monthOfYear, int dayOfMonth){
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        String days;
        if (mMonth + 1 < 10) {
            if (mDay < 10) {
                days = new StringBuffer().append(mYear).append("-").append("0").
                        append(mMonth + 1).append("-").append("0").append(mDay).toString();
            } else {
                days = new StringBuffer().append(mYear).append("-").append("0").
                        append(mMonth + 1).append("-").append(mDay).toString();
            }

        } else {
            if (mDay < 10) {
                days = new StringBuffer().append(mYear).append("-").
                        append(mMonth + 1).append("-").append("0").append(mDay).toString();
            } else {
                days = new StringBuffer().append(mYear).append("-").
                        append(mMonth + 1).append("-").append(mDay).toString();
            }

        }
        return days;
    }

}
