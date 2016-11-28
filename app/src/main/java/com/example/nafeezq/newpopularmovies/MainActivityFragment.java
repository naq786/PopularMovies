package com.example.nafeezq.newpopularmovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A placeholder fragment containing a GridView.
 */
public class MainActivityFragment extends Fragment{

    //Creating ArrayLists for storing Poster images and title/synopsis/rating/release date Strings

    private ArrayList<Bitmap> mPoster = new ArrayList<>();
    private ArrayList<String> titleArray = new ArrayList<>();
    private ArrayList<String> synopsisArray = new ArrayList<>();
    private ArrayList<String> ratingArray = new ArrayList<>();
    private ArrayList<String> releaseArray = new ArrayList<>();
    private ArrayList<String> idArray = new ArrayList<>();


    private GridView gridView;
    private MovieDetailActivityFragment mdafObject = new MovieDetailActivityFragment();


    public MainActivityFragment() {

    }




    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Retrieving and setting default preference value
        PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false);

        //Executing FetchPosterTask AsyncTask
        FetchPosterTask fetch = new FetchPosterTask();
        fetch.execute();

        gridView = (GridView) rootView.findViewById(R.id.gridview_movies);

        //Setting up GridView onclickLister with intents to Movie Detail Activity class

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*
                Below if statement checks if its Tablet for which Two-Pane activity_main layout has flattened.
                If it is a Two pane layout Movie Details are transferred to right hand side Details pane using Movie
                Objects Serialization.
                If it is a single pane layout for phone then Movie Details are transferred using Intent put Extra method.
                 */

            if(getActivity().findViewById(R.id.movie_details_fragments) !=null){


                Bundle mBundle = new Bundle();

                //Movie Class implements Serializable

                Movie movieDetailSerialize = new Movie();


                movieDetailSerialize.setTitle(titleArray.get(position));
                movieDetailSerialize.setSynopsis(synopsisArray.get(position));
                movieDetailSerialize.setRating(ratingArray.get(position));
                movieDetailSerialize.setReleaseDate(releaseArray.get(position));
                movieDetailSerialize.setId(idArray.get(position));

                Bitmap b = mPoster.get(position);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 50, bs);
                movieDetailSerialize.setPoster(bs.toByteArray());

                mBundle.putSerializable("MOVIE_KEY",movieDetailSerialize);

                /*
                Now its checked that details fragment is already attached, if it is already attached
                removed the fragment to avoid Fragment already active exception.
                 */

                FragmentManager fragmentManager = getFragmentManager();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                   if(fragmentManager.findFragmentByTag("DFTAG")!=null) {


                       fragmentTransaction.remove(fragmentManager.findFragmentByTag("DFTAG"));

                   }


                mdafObject.setArguments(mBundle);


                fragmentTransaction.replace(R.id.movie_details_fragments, mdafObject, "DFTAG");

                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();

                    }

            //using Intent to transfer the Movie details to Details Activity if its Phone layout

            else{

                    Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                    Bitmap b = mPoster.get(position);
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 50, bs);
                    intent.putExtra("byteArray", bs.toByteArray());
                    intent.putExtra("titleIntent",titleArray.get(position));
                    intent.putExtra("synopsisIntent",synopsisArray.get(position));
                    intent.putExtra("ratingIntent",ratingArray.get(position));
                    intent.putExtra("releaseIntent",releaseArray.get(position));
                    intent.putExtra("idIntent",idArray.get(position));

                    startActivity(intent);

                    Log.d("IntentActivity", "intent executed");

                }

            }
        });

        return rootView;

    }




    public class FetchPosterTask extends AsyncTask<String, Void, ArrayList<String>> {

        //ArrayList to hold complete URL Strings for different Posters from MovieDB
        private ArrayList<String> urlStringArray = new ArrayList<>();


        public ArrayList<String> getMoviePostersFromJson(String popularMoviesStr) throws JSONException {

            //Checking the preferred movie sorting setting


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortPreference = sp.getString("sorting", "NULL");


            //TreeMaps for Popularity Double Key with List(detailsList)of other String details (title, synopsis etc..)

            Map<Double,List<String>> popularDetailMap = new TreeMap<>();
            Map<Double,List<String>> rateDetailMap = new TreeMap<>();

            //Defining Constants to parse Movie DB JSON Data

            final String MOVIEDB_POSTERPATH = "poster_path";
            final String MOVIEDB_RESULTS = "results";
            final String MOVIEDB_POPULARITY = "popularity";
            final String MOVIEDB_RATE = "vote_average";
            final String MOVIEDB_TITLE = "title";
            final String MOVIEDB_SYNOPSIS = "overview";
            final String MOVIEDB_RELDATE ="release_date";
            final String MOVIEDB_ID = "id";



            JSONObject popularMovies = new JSONObject(popularMoviesStr);
            JSONArray movieArray = popularMovies.getJSONArray(MOVIEDB_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {

                List<String> detailsList = new ArrayList<>();

                JSONObject movie = movieArray.getJSONObject(i);

                String posterPath = movie.getString(MOVIEDB_POSTERPATH);
                Double popu = movie.getDouble(MOVIEDB_POPULARITY);
                Double rate = movie.getDouble(MOVIEDB_RATE);
                String title = movie.getString(MOVIEDB_TITLE);
                String synopsis = movie.getString(MOVIEDB_SYNOPSIS);
                String release = movie.getString(MOVIEDB_RELDATE);
                String id = movie.getString(MOVIEDB_ID);

                detailsList.add(posterPath);
                detailsList.add(title);
                detailsList.add(synopsis);
                detailsList.add(release);
                String rateStr = rate.toString();
                detailsList.add(rateStr);
                detailsList.add(id);


                popularDetailMap.put(popu,detailsList);
                rateDetailMap.put(rate,detailsList);


            }



            switch (sortPreference){

                case "rating":

                    for (Double key : rateDetailMap.keySet()) {

                        List ratingDetails = rateDetailMap.get(key);
                        String ratingPosterPath = (String)ratingDetails.get(0);
                        String movieTitle=(String) ratingDetails.get(1);
                        String movieSynopsis = (String)ratingDetails.get(2);
                        String movieRelease = (String) ratingDetails.get(3);
                        String movieRating = (String)ratingDetails.get(4);
                        String movieID = (String) ratingDetails.get(5);

                        titleArray.add(movieTitle);
                        synopsisArray.add(movieSynopsis);
                        releaseArray.add(movieRelease);
                        ratingArray.add(movieRating);
                        idArray.add(movieID);


                        try {
                            URL posterUrl = new URL("http://image.tmdb.org/t/p/w185/" + ratingPosterPath);
                            String urlString = posterUrl.toString();


                            urlStringArray.add(urlString);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


                    //Reversing the Arrays to have Top Rated and Top Popularity Movies Appear First

                    Collections.reverse(titleArray);
                    Collections.reverse(synopsisArray);
                    Collections.reverse(releaseArray);
                    Collections.reverse(ratingArray);
                    Collections.reverse(idArray);

                    break;

                case "popularity":

                    for (Double key: popularDetailMap.keySet()) {

                        List popularDetails= popularDetailMap.get(key);
                        String popularPosterPath = (String)popularDetails.get(0);

                        String movieTitle=(String) popularDetails.get(1);
                        String movieSynopsis = (String)popularDetails.get(2);
                        String movieRelease = (String) popularDetails.get(3);
                        String movieRating = (String)popularDetails.get(4);
                        String movieID = (String)popularDetails.get(5);


                        titleArray.add(movieTitle);
                        synopsisArray.add(movieSynopsis);
                        releaseArray.add(movieRelease);
                        ratingArray.add(movieRating);
                        idArray.add(movieID);


                        try {
                            URL posterUrl = new URL("http://image.tmdb.org/t/p/w185/" + popularPosterPath);
                            String urlString= posterUrl.toString();

                            urlStringArray.add(urlString);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    //Reversing the Arrays to have Top Rated and Top Popularity Movies Appear First along with mapped movies Details

                    Collections.reverse(titleArray);
                    Collections.reverse(synopsisArray);
                    Collections.reverse(releaseArray);
                    Collections.reverse(ratingArray);
                    Collections.reverse(idArray);

                    break;

            }


            //URL String to populate GridView with Bitmaps in decreasing Popularity or Rating order

                for (int i = urlStringArray.size() - 1; i >= 0; i--) {

                    URL urlToOpen = null;
                    try {
                        urlToOpen = new URL(urlStringArray.get(i));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (urlToOpen != null) {
                            mPoster.add(BitmapFactory.decodeStream(urlToOpen.openConnection().getInputStream()));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                return urlStringArray;
            }

            @Override
              protected ArrayList<String> doInBackground (String...prams){

                //key removed for GitHub upload

                String key = "****************";
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String moviesJsonStr = null;

                try {
                    final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
                    final String APIKEY_PARAM = "api_key";

                    Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                            .appendQueryParameter(APIKEY_PARAM, key)
                            .build();


                    URL url = new URL(builtUri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    moviesJsonStr = buffer.toString();

                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);

                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }

                try {

                    return getMoviePostersFromJson(moviesJsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute (ArrayList result){

                CustomGridAdapter adapter = new CustomGridAdapter(this, mPoster);
                gridView.setAdapter(adapter);

            }

        }

    }

