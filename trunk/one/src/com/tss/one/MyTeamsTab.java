package com.tss.one;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tss.one.listener.MyTeamTabClickListener;
import com.tssoft.one.utils.ElementState;
import com.tssoft.one.utils.Utils;
import com.tssoft.one.webservice.ImageLoader;
import com.tssoft.one.webservice.ImageLoaderStringFactory;
import com.tssoft.one.webservice.WebServiceReaderMyTeam;
import com.tssoft.one.webservice.WebServiceReaderScoreBoard;
import com.tssoft.one.webservice.model.Article;
import com.tssoft.one.webservice.model.ArticleBySubject;
import com.tssoft.one.webservice.model.Game;
import com.tssoft.one.webservice.model.GameBySubject;

public class MyTeamsTab extends MyListActivity {

	private MyTeamTabClickListener tabClickListener = null;
	private NewsAdapter newsAdapter;
	// private ScoreAdapter scoreAdapter;
	// private NewsAdapter newsAdapter;
	private HashMap<Integer, View> chkListNews = new HashMap<Integer, View>();
	private HashMap<Integer, View> chkListGame = new HashMap<Integer, View>();
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Object> newsList = new ArrayList<Object>();
	// private ArrayList<Object> scoreList = new ArrayList<Object>();
	public boolean isGame = true;
	private Runnable viewNews;
	String ARTICLE_KEY = "article";
	String SCORE_KEY = "score";

	private Runnable displayNews = new Runnable() {
		public void run() {
			if (newsList != null && newsList.size() > 0) {
				newsAdapter.notifyDataSetChanged();
			}
			m_ProgressDialog.dismiss();
		}
	};
	private Runnable displayScores = new Runnable() {
		public void run() {
			newsAdapter.notifyDataSetChanged();			
			m_ProgressDialog.dismiss();
		}
	};
	ElementState newsState;
	ElementState gameScoreState;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_teams_tab);
		super.buildMenu(this);

		// ImageButton icon0 = (ImageButton) findViewById(R.id.main_button);
		// ImageButton icon2 = (ImageButton) findViewById(R.id.news_button);
		// ImageButton icon3 = (ImageButton)
		// findViewById(R.id.score_board_button);

		ImageButton tab1 = (ImageButton) findViewById(R.id.my_teams_tab1);
		ImageButton tab2 = (ImageButton) findViewById(R.id.my_teams_tab2);

		ImageButton editTeam = (ImageButton) findViewById(R.id.my_teams_edit);

		// icon0.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View view) {
		// Intent mainDetailIntent = new Intent(view.getContext(),
		// MainList.class);
		// // mainDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		// startActivityForResult(mainDetailIntent, 0);
		// }
		// });
		//
		// icon2.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View view) {
		// Intent newsListIntent = new Intent(view.getContext(),
		// NewsList.class);
		// // newsListIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		// startActivityForResult(newsListIntent, 0);
		// }
		// });

		editTeam.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myTeamsListIntent = new Intent(view.getContext(),
						MyTeamsList.class);
				// newsListIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivityForResult(myTeamsListIntent, 0);
			}
		});
		ImageView refreshIcon = ((ImageView) findViewById(R.id.refrest_icon));
		final MyTeamsTab act = this;
		refreshIcon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
				Intent mainDetailIntent = new Intent(act, MyTeamsTab.class);
				// mainDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(mainDetailIntent);

			}
		});
		// add Tab's Listener to change tab

		newsState = new ElementState(R.drawable.my_teams_tab1,
				R.drawable.my_teams_tab1_over, true);
		gameScoreState = new ElementState(R.drawable.my_teams_tab2,
				R.drawable.my_teams_tab2_over, false);
		HashMap<View, ElementState> elements = new HashMap<View, ElementState>();
		elements.put(findViewById(R.id.my_teams_tab1), newsState);
		elements.put(findViewById(R.id.my_teams_tab2), gameScoreState);
		tabClickListener = new MyTeamTabClickListener(elements, this);
		tab1.setOnClickListener(tabClickListener);
		tab2.setOnClickListener(tabClickListener);
		// Load data

		this.newsAdapter = new NewsAdapter(this, R.layout.my_teams_tab,
				newsList);
		setListAdapter(this.newsAdapter);
		setGameScore();
		// setArticles();
	}

	private class NewsAdapter extends ArrayAdapter<Object> {

		private ArrayList<Object> items;

		public NewsAdapter(Context context, int textViewResourceId,
				ArrayList<Object> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		public void myclear() {
			items.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (isGame) {
				if (chkListGame.containsKey(position))
					return chkListGame.get(position);
			} else {
				if (chkListNews.containsKey(position))
					return chkListNews.get(position);
			}
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Typeface face = Typeface.createFromAsset(getAssets(),
					"fonts/Arial.ttf");

			TextView headline;
			TextView sc;
			Object i = items.get(position);

			if (i instanceof String) {
				v = vi.inflate(R.layout.red_list, null);
				headline = (TextView) v.findViewById(R.id.subject_title);
				headline.setTypeface(face);
				headline.setText((String) i);
			} else if (i instanceof Game) {

				Game game = (Game) i;
				v = vi.inflate(R.layout.my_teams_score_element, null);
				// / All prop
				TextView minute = (TextView) v
						.findViewById(R.id.my_teams_minute);
				ImageView teamLogo1 = (ImageView) v
						.findViewById(R.id.my_teams_logo1);
				TextView name1 = (TextView) v.findViewById(R.id.my_teams_name1);
				TextView score = (TextView) v.findViewById(R.id.my_teams_score);
				TextView name2 = (TextView) v.findViewById(R.id.my_teams_name2);
				ImageView teamLogo2 = (ImageView) v
						.findViewById(R.id.my_teams_logo2);

				// set Value
				minute.setTypeface(face);
				name1.setTypeface(face);
				name2.setTypeface(face);
				score.setTypeface(face);

				minute.setText(game.getStartTime());
				name1.setText(game.getGuestTeam());
				score.setText(game.getGuestScore() + " - "
						+ game.getHomeScore());
				name2.setText(game.getHomeTeam());
				ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,
						SCORE_KEY).setTask(game.getGuestIcon(), teamLogo1);
				ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,
						SCORE_KEY).setTask(game.getHomeIcon(), teamLogo2);
				ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,
						SCORE_KEY).go();

			} else {
				Article article = (Article) i;

				v = vi.inflate(R.layout.white_list, null);
				ImageView imgView = (ImageView) v
						.findViewById(R.id.small_main_image);
				headline = (TextView) v.findViewById(R.id.small_main_headline);
				sc = (TextView) v.findViewById(R.id.small_main_sc);
				headline.setTypeface(face);
				sc.setTypeface(face);

				headline.setText(article.getTitle());
				sc.setText(article.getScTitle());

				ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,
						ARTICLE_KEY).setTask(article.getImageUrl(), imgView);
				ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,
						ARTICLE_KEY).go();

			}
			if (isGame) {
				chkListGame.put(position, v);
			} else {
				chkListNews.put(position, v);
			}

			return v;
		}
	}

	// private class ScoreAdapter extends ArrayAdapter<Object> {
	//
	// private ArrayList<Object> items;
	//
	// public ScoreAdapter(Context context, int textViewResourceId,
	// ArrayList<Object> items) {
	// super(context, textViewResourceId, items);
	// this.items = items;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// if (chkList.containsKey(position))
	// return chkList.get(position);
	//
	// View v = convertView;
	// LayoutInflater vi = (LayoutInflater)
	// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	//
	// Typeface face = Typeface.createFromAsset(getAssets(),
	// "fonts/Arial.ttf");
	// Object i = items.get(position);
	// if (i instanceof String) {
	// v = vi.inflate(R.layout.red_list, null);
	// TextView headline = (TextView) v
	// .findViewById(R.id.subject_title);
	// headline.setTypeface(face);
	// headline.setText((String) i);
	// } else {
	//
	// Game game = (Game) i;
	// v = vi.inflate(R.layout.my_teams_score_element, null);
	// // / All prop
	// TextView minute = (TextView) v
	// .findViewById(R.id.my_teams_minute);
	// ImageView teamLogo1 = (ImageView) v
	// .findViewById(R.id.my_teams_logo1);
	// TextView name1 = (TextView) v.findViewById(R.id.my_teams_name1);
	// TextView score = (TextView) v.findViewById(R.id.my_teams_score);
	// TextView name2 = (TextView) v.findViewById(R.id.my_teams_name2);
	// ImageView teamLogo2 = (ImageView) v
	// .findViewById(R.id.my_teams_logo2);
	//
	// // set Value
	// minute.setTypeface(face);
	// name1.setTypeface(face);
	// name2.setTypeface(face);
	// score.setTypeface(face);
	//
	// minute.setText(game.getStartTime());
	// name1.setText(game.getGuestTeam());
	// score.setText(game.getGuestScore() + " - "
	// + game.getHomeScore());
	// name2.setText(game.getHomeTeam());
	// ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,SCORE_KEY).setTask(
	// game.getGuestIcon(), teamLogo1);
	// ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,SCORE_KEY).setTask(
	// game.getHomeIcon(), teamLogo2);
	// ImageLoaderStringFactory.createImageLoader(MyTeamsTab.this,SCORE_KEY).go();
	//
	// }
	// chkList.put(position, v);
	// return v;
	// }
	// }

	public void getMyteamArticles() {
		try {
			ArrayList<ArticleBySubject> abs = WebServiceReaderMyTeam
					.getUserArrticles(WebServiceReaderMyTeam.getDeviceId(this));
			for (ArticleBySubject a : abs) {
				newsList.add(a.subject);
				newsList.addAll(a.articles);
				// newsList.add("aaa");
			}
			Log.i("ARRAY", "" + newsList.size());
		} catch (UnknownHostException ex) {
			Log.e("Dont have internet ", ex.getMessage());
			Utils.showAlert(this, "No Internet Connection.");
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		runOnUiThread(displayNews);
		if (!isStartNews) {
			isStartNews = true;
			try {
				ImageLoaderStringFactory.createImageLoader(this, ARTICLE_KEY)
						.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void getMyteamScore() {
		try {
			ArrayList<GameBySubject> abs = WebServiceReaderScoreBoard
					.getUserGames(WebServiceReaderMyTeam.getDeviceId(this));
			for (GameBySubject a : abs) {
				newsList.add(a.subject);
				newsList.addAll(a.games);
			}

			Log.i("ARRAY", "" + newsList.size());
		} catch (UnknownHostException ex) {
			Log.e("Dont have internet ", ex.getMessage());
			Utils.showAlert(this, "No Internet Connection.");
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		runOnUiThread(displayScores);
		new ImageLoader(this).start();
		if (!isStartGames) {
			isStartGames = true;
			try {
				ImageLoaderStringFactory.createImageLoader(this, SCORE_KEY)
						.start();
			} catch (IllegalThreadStateException e) {
				e.printStackTrace();
			}
		}

	}

	boolean isStartNews = false;
	boolean isStartGames = false;

	protected void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("Aaaaa");
		if (gameScoreState.isFocused()) {
			Object o = newsList.get(position);
			if (o instanceof Article) {
				MyTeamsNewsDetail.currentArticle = (Article) newsList
						.get(position);
				Intent newsDetailIndent = new Intent(v.getContext(),
						MyTeamsNewsDetail.class);
				startActivityForResult(newsDetailIndent, 0);
			}
		}

	}

	public void setArticles() {
		isGame = false;
		newsList.clear();
		newsAdapter.myclear();
		viewNews = new Runnable() {
			public void run() {
				getMyteamArticles();
			}
		};

		Thread thread = new Thread(viewNews);
		thread.start();
		m_ProgressDialog = ProgressDialog.show(MyTeamsTab.this,
				"Please wait...", "Retrieving data ...", true);
	}

	public void setGameScore() {
		isGame = true;
		newsList.clear();
		newsAdapter.myclear();
		// this.scoreAdapter = new ScoreAdapter(this, R.layout.my_teams_tab,
		// newsList);
		// setListAdapter(this.scoreAdapter);
		viewNews = new Runnable() {
			public void run() {
				getMyteamScore();
			}
		};

		Thread thread = new Thread(viewNews);
		thread.start();
		m_ProgressDialog = ProgressDialog.show(MyTeamsTab.this,
				"Please wait...", "Retrieving data ...", true);

	}
}