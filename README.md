# Android Design Patterns — Java

> A structured summary of MVC → MVP Architecture on Android, using Retrofit, Room, Repository, and LiveData.  
> Written at senior level — every practical gotcha included.

---

## Table of Contents

1. [MVC Architecture Overview](#1-mvc-architecture-overview)
2. [Why MVC Breaks Down on Android — and the Move to MVP](#2-why-mvc-breaks-down-on-android--and-the-move-to-mvp)
3. [Project Structure](#3-project-structure)
4. [The Model Layer](#4-the-model-layer)
5. [The Network Layer (Retrofit + OkHttp)](#5-the-network-layer-retrofit--okhttp)
6. [The Data Source Layer](#6-the-data-source-layer)
7. [Room Database (Local Storage)](#7-room-database-local-storage)
8. [The RecyclerView Adapter](#8-the-recyclerview-adapter)
9. [The Presenter Pattern (MVP)](#9-the-presenter-pattern-mvp)
10. [The Repository Pattern](#10-the-repository-pattern)
11. [LiveData](#11-livedata)
12. [Image Loading with Glide](#12-image-loading-with-glide)
13. [Common Bugs & Senior-Level Notes](#13-common-bugs--senior-level-notes)
14. [Resources & Further Study](#14-resources--further-study)
15. [Architecture Evolution Summary](#15-architecture-evolution-summary)

---

## 1. MVC Architecture Overview

MVC stands for **Model — View — Controller**. It separates your app into three distinct responsibilities:

| Layer | Responsibility | Android Example |
|---|---|---|
| **Model** | Data + business logic | Movie POJO, database operations, API calls |
| **View** | Presentation / UI | XML layouts — only displays data, forwards user events |
| **Controller** | Mediator | Processes user input, updates Model, selects which View to show |

The key benefit: **each layer only knows what it needs to know**.  
The View never talks to the database. The Model never knows what the UI looks like.

---

## 2. Why MVC Breaks Down on Android — and the Move to MVP

In classic MVC, the **Activity acts as the Controller**. The problem? The Activity also *is* the View — it holds references to TextViews, RecyclerViews, and all the UI. This makes it impossible to separate them cleanly.

The result: **Massive Activity** — hundreds of lines mixing network calls, UI updates, business logic, and database access. Impossible to unit test.

```
MVC on Android (the problem):
Activity = Controller + View combined → coupled, untestable, hard to maintain

MVP (the solution):
Activity = pure View (dumb, just renders what it's told)
Presenter = all logic (testable with JUnit, no Android dependency)
Model = data layer (Repository + DataSources)
```

**MVP rule:** The Presenter has zero Android imports (`android.*`). If you see `Context`, `Toast`, or `View` inside a Presenter, something is wrong.

---

## 3. Project Structure

```
app/
├── model/
│   ├── Movie.java                  ← POJO / Room Entity
│   └── MoviesResponse.java         ← API response wrapper
│
├── network/
│   ├── Network.java                ← Retrofit + OkHttp singleton
│   └── MoviesService.java          ← API interface
│
├── datasource/
│   ├── remote/
│   │   ├── MoviesRemoteDataSource.java
│   │   └── MoviesNetworkResponse.java   ← callback interface
│   └── local/
│       ├── MoviesLocalDataSource.java
│       └── dao/
│           └── MoviesDao.java
│
├── db/
│   └── AppDatabase.java            ← Room database singleton
│
├── repository/
│   └── MoviesRepository.java       ← single source of truth
│
└── allmovies/                      ← one package per screen/feature
    ├── AllMoviesActivity.java
    ├── AllMoviesView.java           ← View interface
    ├── AllMoviesPresenter.java      ← Presenter interface
    ├── AllMoviesPresenterImpl.java  ← Presenter logic
    └── MovieAdapter.java           ← RecyclerView adapter
```

> **Rule:** One package per feature (`allmovies`, `favorites`, `moviedetail`). Each feature has its own View interface, Presenter interface, and Presenter implementation.

---

## 4. The Model Layer

### 4.1 — The Movie POJO / Entity

```java
@Entity(tableName = "movies")
public class Movie {

    @PrimaryKey
    @SerializedName("id")  // maps JSON key "id" to this field
    public Long id;

    @SerializedName("title")
    @ColumnInfo(name = "title")
    public String title;

    @SerializedName("poster_path")
    @ColumnInfo(name = "poster_path")
    public String posterPath;

    @SerializedName("original_language")
    @ColumnInfo(name = "original_language")
    public String originalLanguage;

    // Always generate: constructor, getters, setters
    public Movie() {}

    public Movie(Long id, String title, String posterPath) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
}
```

### 4.2 — The API Response Wrapper

The API never returns a plain list. It always wraps it. **This class is what Retrofit actually deserializes.**

```java
public class MoviesResponse {

    @SerializedName("results")
    private List<Movie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public List<Movie> getResults() { return results; }
    public int getTotalPages() { return totalPages; }
}
```

> **Why does this matter?** Retrofit can't deserialize `Call<List<Movie>>` when the JSON root is `{ "results": [...], "page": 1, ... }`. You *must* have a wrapper class. Forgetting this is one of the most common beginner mistakes.

---

## 5. The Network Layer (Retrofit + OkHttp)

### 5.1 — Dependencies

```gradle
// Retrofit
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// OkHttp (logging interceptor — essential for debugging)
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Glide (image loading)
implementation 'com.github.bumptech.glide:glide:4.16.0'
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

### 5.2 — The API Interface

```java
public interface MoviesService {

    // The path is relative to baseUrl — do NOT add a leading "/"
    @GET("discover/movie")
    Call<MoviesResponse> getMovies(@Query("api_key") String apiKey);

    // Example with path parameter
    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(
        @Path("movie_id") long movieId,
        @Query("api_key") String apiKey
    );
}
```

### 5.3 — OkHttp Interceptors (Deep Dive)

📖 **Official docs:** [https://square.github.io/okhttp/features/interceptors/](https://square.github.io/okhttp/features/interceptors/)

An Interceptor sits in the middle of every HTTP request/response. Think of it as middleware — you can **read, modify, or block** any request before it goes out, and any response before it comes in.

```
Your Code → [Application Interceptors] → OkHttp Core → [Network Interceptors] → Server
```

#### The Two Types of Interceptors

| | Application Interceptor | Network Interceptor |
|---|---|---|
| Added with | `addInterceptor()` | `addNetworkInterceptor()` |
| Sees | Every call, including redirects cached responses | Only real network calls |
| Can short-circuit | ✅ Yes (return a fake response) | ❌ No |
| Use for | Auth headers, logging your app's requests, API keys | Modifying headers at transport level, caching |
| Runs even on cache hits | ✅ Yes | ❌ No |

#### Type 1 — Logging Interceptor (from OkHttp docs)

This is the canonical example from the official docs. It measures request timing and logs headers.

```java
class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        // --- Before sending ---
        long t1 = System.nanoTime();
        logger.info(String.format("Sending request %s on %s%n%s",
            request.url(), chain.connection(), request.headers()));

        // Hand off to the next interceptor (or the network)
        Response response = chain.proceed(request);

        // --- After receiving ---
        long t2 = System.nanoTime();
        logger.info(String.format("Received response for %s in %.1fms%n%s",
            response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;   // must always return a response
    }
}
```

The key pattern: **`chain.proceed(request)`** is what actually sends the request. Everything before it runs on the way *out*; everything after runs on the way *in*.

#### Type 2 — API Key Interceptor (Application Interceptor)

Automatically appends the API key to every request URL — no need to add `@Query("api_key")` to every interface method.

```java
class ApiKeyInterceptor implements Interceptor {
    private final String apiKey;

    ApiKeyInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // 1. Get the original request
        Request original = chain.request();

        // 2. Rebuild the URL with the API key added
        HttpUrl newUrl = original.url().newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build();

        // 3. Rebuild the request with the new URL
        Request newRequest = original.newBuilder()
                .url(newUrl)
                .build();

        // 4. Proceed with the modified request
        return chain.proceed(newRequest);
    }
}
```

#### Type 3 — Auth Header Interceptor (Bearer Token)

Real production APIs use `Authorization: Bearer <token>` headers, not query params.

```java
class AuthInterceptor implements Interceptor {
    private final String token;

    AuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request authenticatedRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build();
        return chain.proceed(authenticatedRequest);
    }
}
```

#### Putting It All Together — The Network Singleton

```java
public class Network {

    private static Network instance;
    private Retrofit retrofit;
    private MoviesService moviesService;

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY  = "YOUR_API_KEY_HERE";

    private Network() {
        // Chain multiple interceptors — they run in the order added
        OkHttpClient client = new OkHttpClient.Builder()
                // Application interceptors (run first, even on cache hits)
                .addInterceptor(new ApiKeyInterceptor(API_KEY))
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public MoviesService getMoviesService() {
        if (moviesService == null) {
            moviesService = retrofit.create(MoviesService.class);
        }
        return moviesService;
    }
}
```

> **Senior note — interceptor order matters:** Interceptors run in the order they are added via `addInterceptor()`. Auth → Logging is the right order — you want to log the request *after* auth headers are attached, so the log reflects what actually went out.

---

## 6. The Data Source Layer

**Purpose:** Decouple the Activity from knowing *how* or *where* data comes from. The Activity only knows the data arrived.

### 6.1 — The Callback Interface

Whenever two classes communicate asynchronously, create an interface. This is also the entry point for Dependency Injection.

```java
// Generic version — reusable across features
public interface NetworkResponse<T> {
    void onSuccess(T data);
    void onFailure(String errorMessage);
    void noInternet();
}

// Feature-specific alias (for clarity)
public interface MoviesNetworkResponse extends NetworkResponse<List<Movie>> {}
```

### 6.2 — Remote Data Source

```java
public class MoviesRemoteDataSource {

    private static MoviesRemoteDataSource instance;   // Singleton
    private MoviesService moviesService;

    private MoviesRemoteDataSource() {
        // Use the Network singleton — don't create a new one
        this.moviesService = Network.getInstance().getMoviesService();
    }

    public static synchronized MoviesRemoteDataSource getInstance() {
        if (instance == null) {
            instance = new MoviesRemoteDataSource();
        }
        return instance;
    }

    public void getMovies(MoviesNetworkResponse callback) {
        moviesService.getMovies().enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getResults());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    callback.noInternet();
                } else {
                    callback.onFailure("Unexpected error: " + t.getMessage());
                }
            }
        });
    }
}
```

> **`response.isSuccessful()`** is cleaner than `response.code() == 200` — it checks for any 2xx code.

---

## 7. Room Database (Local Storage)

Room is Android's official SQLite abstraction layer. It validates your SQL queries **at compile time**.

### 7.1 — Dependencies

```gradle
implementation "androidx.room:room-runtime:2.6.1"
annotationProcessor "androidx.room:room-compiler:2.6.1"
```

### 7.2 — The DAO (Data Access Object)

```java
@Dao
public interface MoviesDao {

    // LiveData = Room automatically re-delivers results when data changes
    @Query("SELECT * FROM movies")
    LiveData<List<Movie>> getAllFavorites();

    // Query with a parameter — find a specific movie
    @Query("SELECT * FROM movies WHERE id = :movieId")
    Movie getMovieById(long movieId);

    // OnConflictStrategy.REPLACE = update if already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToFav(Movie movie);

    @Delete
    void deleteFromFav(Movie movie);

    @Query("DELETE FROM movies")
    void deleteAll();
}
```

### 7.3 — The AppDatabase (Thread-Safe Singleton)

```java
@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MoviesDao moviesDao();

    private static volatile AppDatabase INSTANCE;  // volatile = thread-safe visibility

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {      // double-checked locking
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(), // use application context!
                            AppDatabase.class,
                            "moviesDB"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
```

> **Always pass `context.getApplicationContext()`** — not `Activity` context. If you pass the Activity, Room holds a reference to it and causes a memory leak.

### 7.4 — Room Migrations (What happens when you change the schema?)

If you add a column or table and only bump the version number without a migration, the app **crashes** on existing installs.

```java
// Define the migration
static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE movies ADD COLUMN release_date TEXT");
    }
};

// Register it in the builder
INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "moviesDB")
        .addMigrations(MIGRATION_1_2)
        .build();
```

> During **development only**, you can use `.fallbackToDestructiveMigration()` to wipe and rebuild the DB instead of migrating. **Never use this in production.**

### 7.5 — The Local Data Source

```java
public class MoviesLocalDataSource {

    private static MoviesLocalDataSource instance;
    private MoviesDao moviesDao;

    private MoviesLocalDataSource(Context context) {
        // AppDatabase is the singleton — safe to call getInstance repeatedly
        this.moviesDao = AppDatabase.getInstance(context).moviesDao();
    }

    public static synchronized MoviesLocalDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new MoviesLocalDataSource(context);
        }
        return instance;
    }

    // Read — returns LiveData, Room handles threading automatically
    public LiveData<List<Movie>> getFavorites() {
        return moviesDao.getAllFavorites();
    }

    // Writes — MUST run off the main thread
    public void insertMovie(Movie movie) {
        new Thread(() -> moviesDao.addToFav(movie)).start();
    }

    public void deleteMovie(Movie movie) {
        new Thread(() -> moviesDao.deleteFromFav(movie)).start();
    }
}
```

> **Never call `allowMainThreadQueries()`** on the Room builder — it's an escape hatch that exists only for testing. Using it in production blocks the UI thread and causes ANR (App Not Responding) errors.

---

## 8. The RecyclerView Adapter

The Adapter was referenced throughout the lectures but never fully shown. It bridges your data list to the UI.

```java
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList = new ArrayList<>();
    private OnMovieClickListener clickListener;

    // Interface for click events — same callback pattern as everything else
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
        void onFavClick(Movie movie);
    }

    public MovieAdapter(OnMovieClickListener listener) {
        this.clickListener = listener;
    }

    // Called when the Activity/Presenter delivers new data
    public void setMovieList(List<Movie> movies) {
        this.movieList = movies;
        notifyDataSetChanged();
        // Senior tip: use DiffUtil instead of notifyDataSetChanged() for better performance
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.bind(movie, clickListener);
    }

    @Override
    public int getItemCount() { return movieList.size(); }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        ImageView posterImage;
        ImageButton favButton;

        MovieViewHolder(View itemView) {
            super(itemView);
            titleText   = itemView.findViewById(R.id.tv_title);
            posterImage = itemView.findViewById(R.id.iv_poster);
            favButton   = itemView.findViewById(R.id.btn_fav);
        }

        void bind(Movie movie, OnMovieClickListener listener) {
            titleText.setText(movie.getTitle());

            // Load poster with Glide
            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(posterImage);

            itemView.setOnClickListener(v -> listener.onMovieClick(movie));
            favButton.setOnClickListener(v -> listener.onFavClick(movie));
        }
    }
}
```

---

## 9. The Presenter Pattern (MVP)

### Flow

```
User taps button
    → Activity.onClick()
        → presenter.getAllMovies()
            → repository.getAllMovies(callback)
                → remoteDataSource.getMovies(callback)
                    → Retrofit (background thread)
                        → callback.onSuccess(movies)
                            → allMoviesView.showMovies(movies)  ← back on main thread
                                → adapter.setMovieList(movies)
```

### The View Interface

```java
public interface AllMoviesView {
    void showLoading();
    void hideLoading();
    void showMovies(List<Movie> movies);
    void showError(String message);
    void navigateToDetails(Movie movie);
}
```

### The Presenter Interface

```java
public interface AllMoviesPresenter {
    void getAllMovies();
    void onMovieClicked(Movie movie);
    void addToFav(Movie movie);

    // Lifecycle hooks — CRITICAL to prevent memory leaks
    void onAttach(AllMoviesView view);
    void onDetach();
}
```

### The Presenter Implementation

```java
public class AllMoviesPresenterImpl implements AllMoviesPresenter {

    private MoviesRepository moviesRepository;
    private AllMoviesView allMoviesView;   // ← can be null! Always null-check

    public AllMoviesPresenterImpl(Application application) {
        // Presenter creates its own dependencies — or receives them via DI
        this.moviesRepository = new MoviesRepository(application);
    }

    @Override
    public void onAttach(AllMoviesView view) {
        this.allMoviesView = view;
    }

    @Override
    public void onDetach() {
        this.allMoviesView = null;  // ← break the reference, prevent memory leak
    }

    @Override
    public void getAllMovies() {
        if (allMoviesView == null) return;
        allMoviesView.showLoading();

        moviesRepository.getAllMovies(new MoviesNetworkResponse() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (allMoviesView == null) return;  // Activity might be gone
                allMoviesView.hideLoading();
                allMoviesView.showMovies(movies);
            }

            @Override
            public void noInternet() {
                if (allMoviesView == null) return;
                allMoviesView.hideLoading();
                allMoviesView.showError("No internet connection");
            }

            @Override
            public void onFailure(String errorMessage) {
                if (allMoviesView == null) return;
                allMoviesView.hideLoading();
                allMoviesView.showError(errorMessage);
            }
        });
    }

    @Override
    public void onMovieClicked(Movie movie) {
        if (allMoviesView != null) {
            allMoviesView.navigateToDetails(movie);
        }
    }

    @Override
    public void addToFav(Movie movie) {
        moviesRepository.addToFav(movie);
    }
}
```

### The Activity (pure View — no logic)

```java
public class AllMoviesActivity extends AppCompatActivity
        implements AllMoviesView, MovieAdapter.OnMovieClickListener {

    private AllMoviesPresenter presenter;
    private MovieAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_movies);

        RecyclerView recyclerView = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);

        adapter = new MovieAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        presenter = new AllMoviesPresenterImpl((Application) getApplicationContext());
        presenter.onAttach(this);
        presenter.getAllMovies();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();   // ← always detach to prevent memory leak
        super.onDestroy();
    }

    // AllMoviesView callbacks
    @Override public void showLoading()  { progressBar.setVisibility(View.VISIBLE); }
    @Override public void hideLoading()  { progressBar.setVisibility(View.GONE); }
    @Override public void showMovies(List<Movie> movies) { adapter.setMovieList(movies); }
    @Override public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override public void navigateToDetails(Movie movie) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("movie_id", movie.getId());
        startActivity(intent);
    }

    // Adapter click callbacks
    @Override public void onMovieClick(Movie movie) { presenter.onMovieClicked(movie); }
    @Override public void onFavClick(Movie movie)   { presenter.addToFav(movie); }
}
```

---

## 10. The Repository Pattern

The Repository is the **gateway** between the Presenter and the data sources. The Presenter doesn't know if data came from the network or the database — it just asks for data.

```java
public class MoviesRepository {

    private static MoviesRepository instance;
    private MoviesLocalDataSource moviesLocalDataSource;
    private MoviesRemoteDataSource moviesRemoteDataSource;

    private MoviesRepository(Application application) {
        this.moviesLocalDataSource  = MoviesLocalDataSource.getInstance(application);
        this.moviesRemoteDataSource = MoviesRemoteDataSource.getInstance();
    }

    public static synchronized MoviesRepository getInstance(Application application) {
        if (instance == null) {
            instance = new MoviesRepository(application);
        }
        return instance;
    }

    // Fetch from network and optionally cache results
    public void getAllMovies(MoviesNetworkResponse callback) {
        moviesRemoteDataSource.getMovies(new MoviesNetworkResponse() {
            @Override
            public void onSuccess(List<Movie> movies) {
                // Offline-first strategy: cache results in Room automatically
                // new Thread(() -> { for (Movie m : movies) moviesLocalDataSource.insertMovie(m); }).start();
                callback.onSuccess(movies);
            }

            @Override
            public void noInternet() {
                // Fallback: return cached data from Room
                callback.noInternet();
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void addToFav(Movie movie)    { moviesLocalDataSource.insertMovie(movie); }
    public void deleteFromFav(Movie movie) { moviesLocalDataSource.deleteMovie(movie); }

    // LiveData — direct pass-through from Room
    public LiveData<List<Movie>> getFavorites() {
        return moviesLocalDataSource.getFavorites();
    }
}
```

---

## 11. LiveData

`LiveData` is a **lifecycle-aware observable**. It only updates observers (Activities/Fragments) when they are in an active state (started or resumed). If the Activity is destroyed, it won't deliver updates — no crashes, no leaks.

### How it works

```java
// 1. DAO returns LiveData — Room emits a new value whenever the table changes
@Query("SELECT * FROM movies")
LiveData<List<Movie>> getAllFavorites();

// 2. Presenter exposes it
public LiveData<List<Movie>> getFavorites() {
    return moviesRepository.getFavorites();
}

// 3. Activity observes it — `this` is the LifecycleOwner
presenter.getFavorites().observe(this, movies -> {
    adapter.setMovieList(movies);
});
```

### LiveData vs StateFlow vs SharedFlow

| | LiveData | StateFlow | SharedFlow |
|---|---|---|---|
| Language | Java & Kotlin | Kotlin only | Kotlin only |
| Lifecycle-aware | ✅ built-in | ❌ manual (`repeatOnLifecycle`) | ❌ manual |
| Initial value required | ❌ | ✅ | ❌ |
| Replays last value to new observer | ✅ | ✅ | configurable |
| Hot or Cold | Hot | Hot | Hot |
| Best for | Android Views (Java) | ViewModel state (Kotlin) | one-time events |

> For Java projects: use **LiveData**. For modern Kotlin projects: use **StateFlow** for state, **SharedFlow** for one-shot events (navigation, snackbars).

---

## 12. Image Loading with Glide

Glide handles the full image loading pipeline: background download, caching (memory + disk), decoding, and displaying.

```java
Glide.with(context)
        .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
        .placeholder(R.drawable.ic_placeholder)   // shown while loading
        .error(R.drawable.ic_error)               // shown if load fails
        .centerCrop()
        .into(imageView);
```

> **Never load images on the main thread.** Glide automatically dispatches network/disk work to background threads.

---

## 13. Common Bugs & Senior-Level Notes

### ⚠️ Memory Leak — Presenter holding View reference

```java
// BAD — if the Activity is rotated/destroyed while the network call is in-flight,
// the Presenter still holds the old Activity. Garbage Collector can't collect it.
public class PresenterImpl {
    private AllMoviesView view;  // strong reference = potential leak
}

// FIX — always null the view in onDetach()
@Override
public void onDetach() {
    this.allMoviesView = null;
}

// Also: always null-check before using the view
@Override
public void onSuccess(List<Movie> movies) {
    if (allMoviesView == null) return;
    allMoviesView.showMovies(movies);
}
```

### ⚠️ Network on Main Thread

```
android.os.NetworkOnMainThreadException
```

Any HTTP call must use `enqueue()` (Retrofit's async method). Never call `.execute()` from the main thread.

### ⚠️ Room on Main Thread

```
java.lang.IllegalStateException: Cannot access database on the main thread
```

All Room write operations (`@Insert`, `@Delete`, `@Update`, `@Query` that writes) must run on a background thread. Room's `LiveData` reads are automatically off-thread. Plain `List<>` reads need a background thread too.

### ⚠️ Non-Thread-Safe Singleton

```java
// BAD — two threads can both pass the null check simultaneously
if (INSTANCE == null) {
    INSTANCE = new AppDatabase(...);
}

// GOOD — synchronized + volatile double-checked locking
private static volatile AppDatabase INSTANCE;

public static AppDatabase getInstance(Context context) {
    if (INSTANCE == null) {
        synchronized (AppDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(...).build();
            }
        }
    }
    return INSTANCE;
}
```

### ⚠️ Wrong Context in Singletons

```java
// BAD — holds reference to Activity, leaks it
INSTANCE = new MoviesLocalDataSource(activityContext);

// GOOD — Application context lives as long as the app
INSTANCE = new MoviesLocalDataSource(context.getApplicationContext());
```

### ⚠️ baseUrl must end with "/"

```java
// BAD
.baseUrl("https://api.themoviedb.org/3")

// GOOD
.baseUrl("https://api.themoviedb.org/3/")
```

### ⚠️ Leading "/" in @GET path

```java
// BAD — the leading slash makes Retrofit ignore the baseUrl path
@GET("/discover/movie")

// GOOD — relative path, appended to baseUrl
@GET("discover/movie")
```

---

## 14. Resources & Further Study

### 📚 Official Documentation

| Topic | Link |
|---|---|
| Android Architecture Guide | [developer.android.com/topic/architecture](https://developer.android.com/topic/architecture) |
| OkHttp Interceptors | [square.github.io/okhttp/features/interceptors](https://square.github.io/okhttp/features/interceptors/) |
| Retrofit | [square.github.io/retrofit](https://square.github.io/retrofit/) |
| Room Persistence Library | [developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room) |
| Room Migrations | [developer.android.com/training/data-storage/room/migrating-db-versions](https://developer.android.com/training/data-storage/room/migrating-db-versions) |
| LiveData Overview | [developer.android.com/topic/libraries/architecture/livedata](https://developer.android.com/topic/libraries/architecture/livedata) |
| Glide | [bumptech.github.io/glide](https://bumptech.github.io/glide/) |
| Guide to App Architecture | [developer.android.com/topic/architecture/recommendations](https://developer.android.com/topic/architecture/recommendations) |

---

### 🔬 Concepts to Research Yourself

| Topic | Why It Matters |
|---|---|
| **Java Reflection** | How `retrofit.create(MoviesService.class)` implements the interface at runtime without you writing the code |
| **Annotation Processing (APT)** | How `@GET`, `@Entity`, `@Dao` generate code at compile time — Room's DAO impl is generated this way |
| **Bearer Token Auth** | Production APIs use `Authorization: Bearer <token>` headers — see the `AuthInterceptor` in section 5.3 |
| **Generic Classes in Java** | Build a reusable `NetworkResponse<T>` instead of one callback interface per feature |
| **DiffUtil** | Smarter RecyclerView updates — only re-renders changed items instead of the whole list |
| **Offline-First Strategy** | Show cached Room data instantly on open, refresh from network in background — the Facebook approach |
| **State Holders** | The formal Android architecture concept: anything that owns and survives UI state |
| **ViewModel (MVVM)** | The next evolution after MVP — the ViewModel survives configuration changes (rotation) automatically |
| **Hilt / Dagger** | Dependency Injection framework — eliminates manual Singleton management entirely |
| **LiveData vs StateFlow vs SharedFlow** | See comparison table in section 11 |
| **NetworkBoundResource** | Google's pattern for offline-first: always emit from DB, trigger network refresh, emit loading/success/error |

---

## 15. Architecture Evolution Summary

```
Step 1: Everything in Activity
        ← messy, untestable, impossible to maintain

Step 2: + DataSource layer (Remote + Local)
        ← business logic out of Activity, but Activity still coordinates everything

Step 3: + Room (LocalDataSource)
        ← offline support, but write operations still on main thread (bug!)

Step 4: + Presenter (MVP) with onAttach/onDetach
        ← Activity is now a dumb View, Presenter is testable with pure JUnit

Step 5: + Repository
        ← single source of truth per feature, Presenter doesn't know where data comes from

Step 6: + LiveData
        ← reactive UI, no manual threads for reads, lifecycle-safe
```

**Each step is a trade-off:**

| Pattern | Pro | Con |
|---|---|---|
| MVC | Simple to start | Activity does everything |
| MVP | Testable Presenter | Manual memory management (attach/detach) |
| MVVM + ViewModel | Survives rotation automatically | Kotlin-oriented, more boilerplate |
| MVVM + Hilt | Full DI, no Singletons | Steep learning curve |

The architecture you saw in these lectures is **MVP + Repository + LiveData** — a solid, battle-tested combination used in production Java Android apps.
