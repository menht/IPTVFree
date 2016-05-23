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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.michelelacorte.iptvfree.MainActivity;
import it.michelelacorte.iptvfree.m3u.M3UData;
import it.michelelacorte.iptvfree.R;
import it.michelelacorte.iptvfree.fast_scroller.BubbleTextGetter;
import it.michelelacorte.iptvfree.fast_scroller.FastScroller;
import it.michelelacorte.iptvfree.util.Utils;

/**
 * Created by Michele on 20/04/2016.
 */
public class FragmentIPTV extends Fragment {
    RecyclerView rv;
    LinearLayoutManager llm;
    CardViewAdapter adapter;
    private FastScroller fastScroller;
    private List<String> channelLink = new ArrayList<>();
    private List<String> channelName = new ArrayList<>();
    private List<M3UData> m3uDatas = new ArrayList<>();

    public static FragmentIPTV newInstance(ArrayList<String> channelNames, ArrayList<String> channelLinks) {
        FragmentIPTV fragmentIPTV = new FragmentIPTV();
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
        m3uDatas = Utils.convertToM3UData(channelName, channelLink);
        adapter = new CardViewAdapter(m3uDatas, getContext());
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
class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> implements BubbleTextGetter {
    private List<M3UData> m3UData;
    private Context context;

    public CardViewAdapter(List<M3UData> m3UDatas, Context context) {
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
        if(m3UData == null)
        {
            viewHolder.channelLink.setText("Non hai preferiti!");
            viewHolder.channelName.setText("Non hai preferiti!");
        }else {
            viewHolder.channelLink.setText(m3UData.get(i).getChannelLink());
            viewHolder.channelName.setText(m3UData.get(i).getChannelName());
        }
        viewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m3UData.get(i).getChannelLink()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                try {
                    URL domainUrl = new URL(m3UData.get(i).getChannelLink());
                    Toast.makeText(context, context.getResources().getString(R.string.disclaimer_link)
                            + " " + domainUrl.getHost(), Toast.LENGTH_SHORT).show();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getResources().getString(R.string.disclaimer_link)
                            + " " + m3UData.get(i).getChannelLink(), Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public int getItemCount() {
        return m3UData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView channelName;
        private TextView channelLink;
        private ImageView channelImage;
        private Toolbar toolbar;
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
                    if (id == R.id.action_signal) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getResources().getString(R.string.email)});
                        i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.email_subject));
                        i.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.email_text) + "\n" + channelLink.getText());
                        try {
                            context.startActivity(Intent.createChooser(i, context.getResources().getString(R.string.email_send)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(context, context.getResources().getString(R.string.email_failed), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    if(id == R.id.action_favorite)
                    {
                        MainActivity.favoriteLink.add(channelLink.getText().toString());
                        MainActivity.favoriteName.add(channelName.getText().toString());
                       // FragmentIPTVFavorite.adapter.addItem(0, new M3UData(channelName.getText().toString(), channelLink.getText().toString()));
                    }

                    return false;
                }
            };

            toolbar.inflateMenu(R.menu.menu_main);
            toolbar.setOnMenuItemClickListener(toolbarListener);

        }
    }
}