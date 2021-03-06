package com.swsdkj.wsl.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swsdkj.wsl.R;
import com.swsdkj.wsl.activity.ImagePagerActivity;
import com.swsdkj.wsl.activity.PlayVideo;
import com.swsdkj.wsl.bean.SignBean;
import com.swsdkj.wsl.net.BaseUrl;
import com.swsdkj.wsl.tool.StringTool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者： 关云秀 on 2016/12/15.
 * 描述：
 */
public class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private Context context;
    private List<SignBean> signBeanList;

    public SignAdapter(Context context, List<SignBean> signBeanLis) {
        this.context = context;
        this.signBeanList = signBeanLis;
    }


    @Override
    public int getItemCount() {
        int aa = signBeanList.size();
        return aa;

    }

    @Override
    public int getItemViewType(int position) {

            return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //加载每个列表项
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_signyes, parent, false);
            return new MyHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //加载每个列表项的数据
        if (holder instanceof MyHolder){
            final SignBean signBean = signBeanList.get(position);
            if (signBean!=null){

                ((MyHolder)holder).addressTV.setText("签到地点: "+signBean.getAddress());
                if (!StringTool.getString2Front(signBean.getUser().getName()).equals("")){
                    ((MyHolder)holder).iconTV.setText(StringTool.getString2Front(signBean.getUser().getName())+"");
                    ((MyHolder)holder).nameTV.setText(signBean.getUser().getName()+"");
                }
                if (!TextUtils.isEmpty(signBean.getPhoto1())){
                    ((MyHolder) holder).img1Flv.setVisibility(View.VISIBLE);
                    Log.i("123",signBean.getPhoto1()+"------------截取字符串------1---------");
                    final String str1=signBean.getPhoto1().substring(signBean.getPhoto1().indexOf(".")+1, signBean.getPhoto1().length());
                    if (str1.equals("mp4")){
                        ((MyHolder)holder).playImg1.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(BaseUrl.UMAGE_URL + signBean.getFace1()).fit()
                                .into(((MyHolder) holder).imageView1);

                    }else {
                        ((MyHolder) holder).playImg1.setVisibility(View.GONE);
                        Picasso.with(context)
                                .load(BaseUrl.UMAGE_URL + signBean.getPhoto1())
                                .into(((MyHolder) holder).imageView1);
                    }
                    //视频播放
                    ((MyHolder)holder).imageView1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (str1.equals("mp4")) {
                                Intent intent = new Intent(context, PlayVideo.class);
                                intent.putExtra("path", BaseUrl.UMAGE_URL + signBean.getPhoto1());
                                context.startActivity(intent);
                            }else{
                                ArrayList<String> urls = new ArrayList<>();
                                urls.add(BaseUrl.UMAGE_URL + signBean.getPhoto1());
                                Intent intent = new Intent(context, ImagePagerActivity.class);
                                // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
                                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
                                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, 0);
                                intent.putExtra("flag", "0");
                                context.startActivity(intent);
                            }
                        }
                    });


                }else{
                    ((MyHolder) holder).img1Flv.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(signBean.getPhoto2())){
                    ((MyHolder) holder).img2Flv.setVisibility(View.VISIBLE);
                    Log.i("123",signBean.getPhoto2()+"------------截取字符串------2---------");
                    final String str2=signBean.getPhoto2().substring(signBean.getPhoto2().indexOf(".")+1, signBean.getPhoto2().length());
                    if (str2.equals("mp4")) {
                           /* if(TextUtils.isEmpty(signBean.getPhoto1())) {
                                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                                        (int) context.getResources().getDimension(R.dimen.image_height), (int) context.getResources().getDimension(R.dimen.image_height));
                                ((MyHolder) holder).img2Flv.setLayoutParams(layout);
                            }else{
                                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                ((MyHolder) holder).img2Flv.setLayoutParams(layout);
                            }*/
                        ((MyHolder) holder).playImg2.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(BaseUrl.UMAGE_URL + signBean.getFace2()).fit()
                                .into(((MyHolder) holder).imageView2);

                    } else {
                           /* if(TextUtils.isEmpty(signBean.getPhoto1())) {
                                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                                        (int) context.getResources().getDimension(R.dimen.image_height), (int) context.getResources().getDimension(R.dimen.image_height));
                                ((MyHolder) holder).img2Flv.setLayoutParams(layout);
                            }else{
                                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                ((MyHolder) holder).img2Flv.setLayoutParams(layout);
                            }*/
                        ((MyHolder) holder).playImg2.setVisibility(View.GONE);
                        Picasso.with(context)
                                .load(BaseUrl.UMAGE_URL + signBean.getPhoto2()).fit()
                                .into(((MyHolder) holder).imageView2);

                    }
                    //视频播放
                    ((MyHolder)holder).imageView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (str2.equals("mp4")) {
                                Intent intent = new Intent(context, PlayVideo.class);
                                intent.putExtra("path", BaseUrl.UMAGE_URL + signBean.getPhoto2());
                                context.startActivity(intent);
                            }else{
                                ArrayList<String> urls = new ArrayList<>();
                                urls.add(BaseUrl.UMAGE_URL + signBean.getPhoto2());
                                Intent intent = new Intent(context, ImagePagerActivity.class);
                                // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
                                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
                                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, 0);
                                intent.putExtra("flag", "0");
                                context.startActivity(intent);
                            }
                        }
                    });


                }else{
                    if(!TextUtils.isEmpty(signBean.getPhoto1())) {
                        ((MyHolder) holder).img2Flv.setVisibility(View.INVISIBLE);
                    }else{
                        ((MyHolder) holder).img2Flv.setVisibility(View.GONE);
                    }
                }

                ((MyHolder)holder).signTV.setText(signBean.getTime()+"今日已经签到"+"1次");
                ((MyHolder)holder).addressTV.setText("签到地点: "+signBean.getAddress());
                if (signBean.getContext()!=null){
                    ((MyHolder)holder).remarksTV.setText("备注: "+signBean.getContext());
                    ((MyHolder)holder).remarksTV.setVisibility(View.VISIBLE);
                }else {
                    ((MyHolder)holder).remarksTV.setVisibility(View.GONE);
                }

            }
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView iconTV,signTV,nameTV,addressTV,remarksTV;
        ImageView imageView1,imageView2,playImg1,playImg2;
        LinearLayout layout;
        FrameLayout img1Flv,img2Flv;
        public MyHolder(View itemView) {
            super(itemView);
            iconTV = (TextView)itemView.findViewById(R.id.id_icon);
            nameTV = (TextView)itemView.findViewById(R.id.id_name);
            signTV = (TextView)itemView.findViewById(R.id.id_sign);
            addressTV = (TextView)itemView.findViewById(R.id.id_address);
            remarksTV = (TextView)itemView.findViewById(R.id.id_remarks);


            imageView1 = (ImageView)itemView.findViewById(R.id.id_image1);
            imageView2 = (ImageView)itemView.findViewById(R.id.id_image2);

            layout = (LinearLayout)itemView.findViewById(R.id.id_lay);
            img1Flv = (FrameLayout)itemView.findViewById(R.id.id_image1_flv);
            img2Flv = (FrameLayout)itemView.findViewById(R.id.id_image2_flv);
            playImg1 = (ImageView)itemView.findViewById(R.id.id_play_img1);
            playImg2 = (ImageView)itemView.findViewById(R.id.id_play_img2);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.foot_text)
        TextView footText;

        FootViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
