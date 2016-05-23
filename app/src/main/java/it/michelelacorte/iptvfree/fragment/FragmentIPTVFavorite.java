package it.michelelacorte.iptvfree.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.michelelacorte.iptvfree.MainActivity;
import it.michelelacorte.iptvfree.m3u.M3UData;
import it.michelelacorte.iptvfree.R;
import it.michelelacorte.iptvfree.fast_scroller.BubbleTextGetter;
import it.michelelacorte.iptvfree.fast_scroller.FastScroller;
import it.michelelacorte.iptvfree.util.Utils;

/**
 * Created by Michele on 23/04/2016.
 */
public class FragmentIPTVFavorite  extends Fragment {
    RecyclerView rv;
    LinearLayoutManager llm;
    private FastScroller fastScroller;
    public static CardViewAdapterFavorite adapter;
    private List<String> channelLink = new ArrayList<String>();
    private List<String> channelName = new ArrayList<String>();
    private List<M3UData> m3UDatas = new ArrayList<>();

    public static FragmentIPTVFavorite newInstance(ArrayList<String> channelNames, ArrayList<String> channelLinks) {
        FragmentIPTVFavorite fragmentIPTV = new FragmentIPTVFavorite();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("channels", channelNames);
        bundle.putStringArrayList("links", channelLinks);
        fragmentIPTV.setArguments(bundle);
        return fragmentIPTV;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null && getArguments().getStringArrayList("links") != null && getArguments().getStringArrayList("channels") != null) {
            channelLink = getArguments().getStringArrayList("links");
            channelName = getArguments().getStringArrayList("channels");
        }
        return inflater.inflate(R.layout.fragment_iptv, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        rv = (RecyclerView) view.findViewById(R.id.rv);
        llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        m3UDatas = Utils.convertToM3UData(channelName, channelLink);
        adapter = new CardViewAdapterFavorite(m3UDatas, getContext());
        rv.setAdapter(adapter);
        fastScroller=(FastScroller) view.findViewById(R.id.fastscroller);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        fastScroller.setRecyclerView(rv);
    }
}

/**
 * A simple adapter that loads a CardView layout, and
 * listens to clicks on the Buttons or on the CardView, it implements
 * BubbleTextGetter that provide fast scroller animation
 */
class CardViewAdapterFavorite extends RecyclerView.Adapter<CardViewAdapterFavorite.ViewHolder>  implements BubbleTextGetter {
    private List<M3UData> m3UData;
    private Context context;

    public CardViewAdapterFavorite(List<M3UData> m3UDatas, Context context) {
        this.m3UData = m3UDatas;
        this.context = context;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return m3UData.get(pos).getChannelName().substring(0, 1).toUpperCase();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder,final int i) {
        if(m3UData == null) {
            viewHolder.channelLink.setText("ASD");
            viewHolder.channelName.setText("ADS");
        }else{
            viewHolder.channelLink.setText(m3UData.get(i).getChannelLink());
            viewHolder.channelName.setText(m3UData.get(i).getChannelName());
        }
        viewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m3UData.get(i).getChannelLink()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        Picasso.with(context)
                .load("http://iptvfree.altervista.org/IconStandard/" + viewHolder.channelName.getText().toString().replace(' ', '_') + ".png")
                .resize(200, 150)
                .into(viewHolder.channelImage, new Callback(){
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context)
                                .load("http://iptvfree.altervista.org/IconStandard/" + viewHolder.channelName.getText().toString().replace(' ', '_') + ".jpg")
                                .resize(200, 150)
                                .into(viewHolder.channelImage, new Callback(){
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        viewHolder.channelImage.setImageResource(android.R.drawable.ic_dialog_alert);
                                    }
                                });
                    }
                });
    }

    public void addItem(int position, M3UData data) {
        m3UData.add(position, data);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        m3UData.remove(position);
        MainActivity.favoriteLink.remove(position);
        MainActivity.favoriteName.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, m3UData.size());
    }

    @Override
    public int getItemCount() {
        return m3UData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView channelName;
        private TextView channelLink;
        private Toolbar toolbar;
        private ImageView channelImage;
        private CardView cv;

        public ViewHolder(final View itemView) {
            super(itemView);
            channelName = (TextView) itemView.findViewById(R.id.channelName);
            channelLink = (TextView) itemView.findViewById(R.id.channelLink);
            channelImage = (ImageView) itemView.findViewById(R.id.channelImage);
            toolbar = (Toolbar) itemView.findViewById(R.id.toolbar);
            cv = (CardView)itemView.findViewById(R.id.cv);
            Toolbar.OnMenuItemClickListener toolbarListener = new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_delete) {
                        removeItem(getAdapterPosition());
                        return true;
                    }

                    return false;
                }
            };

            toolbar.inflateMenu(R.menu.menu_favorite);
            toolbar.setOnMenuItemClickListener(toolbarListener);
        }
    }
}