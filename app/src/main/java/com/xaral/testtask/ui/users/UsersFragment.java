package com.xaral.testtask.ui.users;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xaral.testtask.R;
import com.xaral.testtask.adapters.UsersViewAdapter;
import com.xaral.testtask.api.TestAssignmentApi;
import com.xaral.testtask.api.User;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView usersView;
    private UsersViewAdapter usersViewAdapter;
    private ConstraintLayout noUsersView, errorView;
    private TextView reconnect;
    private int userCount;
    private int nextPage;
    private boolean isLoading;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_users, container, false);

        userCount = 0;
        nextPage = 1;
        isLoading = true;

        usersView = root.findViewById(R.id.usersView);
        noUsersView = root.findViewById(R.id.noUsersView);
        errorView = root.findViewById(R.id.errorView);
        reconnect = root.findViewById(R.id.reconnect);

        // Set click listener for reconnect button
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reconnect.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.disabled_primary_button));
                reconnect.setTextColor(ContextCompat.getColor(view.getContext(), R.color.grey));
                if (!isLoading)
                    loadMoreData(root.getContext(), nextPage);
            }
        });

        usersViewAdapter = new UsersViewAdapter(root.getContext(), new ArrayList<>());
        usersView.setAdapter(usersViewAdapter);

        loadMoreData(root.getContext(), nextPage);

        LinearLayoutManager layoutManager = (LinearLayoutManager) usersView.getLayoutManager();

        // Set scroll listener for RecyclerView to load more data when reaching the end
        usersView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == userCount) {
                    loadMoreData(root.getContext(), nextPage);
                    isLoading = true;
                }
            }
        });
        return root;
    }

    /**
     * Loads more user data from the API.
     *
     * @param context the context to use for UI operations
     * @param page    the page number to load
     */
    private void loadMoreData(Context context, int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<User> users;
                    users = TestAssignmentApi.getUsers(page, 6);

                    if (users == null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                errorView.setVisibility(View.VISIBLE);
                                reconnect.setBackground(ContextCompat.getDrawable(context, R.drawable.primary_button_background));
                                reconnect.setTextColor(ContextCompat.getColor(context, R.color.black));
                                isLoading = false;
                            }
                        });
                        return;
                    }

                    if (users.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                usersViewAdapter.setFull();
                                isLoading = false;
                            }
                        });
                        return;
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            userCount += users.size();
                            usersViewAdapter.addUsers(users);
                            if (userCount > 0)
                                noUsersView.setVisibility(View.INVISIBLE);
                            errorView.setVisibility(View.INVISIBLE);
                            nextPage++;
                        }
                    });
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            errorView.setVisibility(View.VISIBLE);
                            reconnect.setBackground(ContextCompat.getDrawable(context, R.drawable.primary_button_background));
                            reconnect.setTextColor(ContextCompat.getColor(context, R.color.black));
                            isLoading = false;
                        }
                    });
                }
                finally {
                    isLoading = false;
                }
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}