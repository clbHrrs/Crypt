package com.a02094311.crypt;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.a02094311.crypt.databinding.FragmentVaultBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VaultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VaultFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentVaultBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String masterPassword;
    private Cipher cipher = null;
    private Cipher cipher1 = null;
    private Cipher cipher2 = null;
    private int totalPasswords = 0;
    Adapter adapter;
    List<card> list = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VaultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VaultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VaultFragment newInstance(String param1, String param2) {
        VaultFragment fragment = new VaultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        totalPasswords = 0;
        FirebaseUser user = mAuth.getCurrentUser();

        binding.vaultLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(binding.getRoot()).
                    navigate(VaultFragmentDirections.
                            actionVaultFragmentToLoginFragment());
        });

        binding.addPasswordPairButton.setOnClickListener(view -> {
            Navigation.findNavController(binding.getRoot()).
                    navigate((VaultFragmentDirections.
                            actionVaultFragmentToAddData()).setPassword(masterPassword));
        });

        binding.vaultSettings.setOnClickListener(view ->{
            Navigation.findNavController(binding.getRoot()).
                    navigate(VaultFragmentDirections.
                            actionVaultFragmentToSettingsFragment());
        });

        //binding.content.removeAllViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentVaultBinding.inflate(getLayoutInflater());
        masterPassword = VaultFragmentArgs.fromBundle(getArguments()).getPassword();
        binding.vaultError.setText("Fetching passwords...");
        binding.vaultError.setVisibility(View.VISIBLE);
        FirebaseUser user = mAuth.getCurrentUser();
        db.collection(user.getUid()).
                get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() <= 1) {
                                binding.vaultError.setText("You have no stored usernames or passwords.");
                            }
                            else {
                                for (QueryDocumentSnapshot data : task.getResult()) {
                                    try {
                                        if (data.getString("username") != null) {
                                            String salt = data.getString("titleSalt");
                                            String salt1 = data.getString("passwordSalt");
                                            String salt2 = data.getString("usernameSalt");
                                            byte[] byteSalt = Base64.decode(salt, Base64.DEFAULT);
                                            byte[] byteSalt1 = Base64.decode(salt1, Base64.DEFAULT);
                                            byte[] byteSalt2 = Base64.decode(salt2, Base64.DEFAULT);
                                            String titleCipher = data.getString("title");
                                            String usernameCipher = data.getString("username");
                                            String passwordCipher = data.getString("password");

                                            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), byteSalt, 65536, 128);
                                            KeySpec spec1 = new PBEKeySpec(masterPassword.toCharArray(), byteSalt1, 65536, 128);
                                            KeySpec spec2 = new PBEKeySpec(masterPassword.toCharArray(), byteSalt2, 65536, 128);
                                            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                                            byte[] keyBytes = f.generateSecret(spec).getEncoded();
                                            byte[] keyBytes1 = f.generateSecret(spec1).getEncoded();
                                            byte[] keyBytes2 = f.generateSecret(spec2).getEncoded();
                                            SecretKeySpec secret = new SecretKeySpec(keyBytes, "AES");
                                            SecretKeySpec secret1 = new SecretKeySpec(keyBytes1, "AES");
                                            SecretKeySpec secret2 = new SecretKeySpec(keyBytes2, "AES");
                                            cipher = null;
                                            cipher1 = null;
                                            cipher2 = null;
                                            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                                            cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                                            cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                                            cipher.init(Cipher.DECRYPT_MODE, secret);
                                            cipher1.init(Cipher.DECRYPT_MODE, secret1);
                                            cipher2.init(Cipher.DECRYPT_MODE, secret2);

                                            byte[] titleCipherBytes = Base64.decode(titleCipher, Base64.DEFAULT);
                                            byte[] usernameCipherBytes = Base64.decode(usernameCipher, Base64.DEFAULT);
                                            byte[] passwordCipherBytes = Base64.decode(passwordCipher, Base64.DEFAULT);

                                            String titlePlain = new String(cipher.doFinal(titleCipherBytes), "UTF-8");
                                            String usernamePlain = new String(cipher1.doFinal(usernameCipherBytes), "UTF-8");
                                            String passwordPlain = new String(cipher2.doFinal(passwordCipherBytes), "UTF-8");
                                            list.add(new card(titlePlain, passwordPlain, usernamePlain, data.getId()));
                                            adapter.notifyItemInserted(totalPasswords);
                                            totalPasswords++;
                                            binding.vaultError.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (Exception e) {
                                        Log.e("exception", e.toString());
                                    }
                                }
                            }
                        }
                    }
                });
        adapter = new Adapter(binding.getRoot().getContext(), list);
        binding.content.setAdapter(adapter);
        LinearLayoutManager lay = new LinearLayoutManager(binding.getRoot().getContext());
        binding.content.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.content.getContext(),
                lay.getOrientation());
        binding.content.addItemDecoration(dividerItemDecoration);
        AdView mAdView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        return binding.getRoot();
    }
    /*
    public class card {
        String username;
        String password;
        CardView card;
        LinearLayout layoutVertical;
        LinearLayout layoutHorizontalUsername;
        LinearLayout layoutHorizontalPassword;

        TextView usernameView;
        TextView passwordView;

        Button usernameCopy;
        Button passwordCopy;

        public card(String username, String password, View view) {
            this.username = username;
            this.password = password;

            card = new CardView(view.getContext());
            card.setMinimumHeight(300);
            card.setMinimumWidth(100);
            card.setRadius(10);
            card.setCardBackgroundColor(Color.CYAN);
            card.setCardElevation(10);

            layoutVertical = new LinearLayout(view.getContext());
            layoutVertical.setOrientation(LinearLayout.VERTICAL);
            layoutVertical.setGravity(Gravity.CENTER);
            layoutVertical.setMinimumHeight(card.getMinimumWidth());
            card.addView(layoutVertical);

            layoutHorizontalUsername = new LinearLayout(view.getContext());
            layoutHorizontalUsername.setOrientation(LinearLayout.HORIZONTAL);
            layoutHorizontalUsername.setMinimumHeight(card.getMinimumWidth());
            //layoutHorizontalUsername.setGravity(Gravity.CENTER);
            layoutVertical.addView(layoutHorizontalUsername);

            usernameView = new TextView(view.getContext());
            usernameView.setText("Username: " + this.username);
            usernameView.setGravity(Gravity.START);
            usernameView.setGravity(Gravity.CENTER);
            layoutHorizontalUsername.addView(usernameView);

            usernameCopy = new Button(view.getContext());
            usernameCopy.setText("Copy to Clipboard");
            usernameCopy.setGravity(Gravity.END);
            usernameCopy.setBackgroundColor(Color.GRAY);
            usernameCopy.setOnClickListener(v ->{
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("username", this.username);
                clipboard.setPrimaryClip(clip);
            });
            layoutHorizontalUsername.addView(usernameCopy);

            layoutHorizontalPassword = new LinearLayout(view.getContext());
            layoutHorizontalPassword.setOrientation(LinearLayout.HORIZONTAL);
            layoutHorizontalPassword.setMinimumHeight(card.getMinimumWidth());
            //layoutHorizontalPassword.setGravity(Gravity.CENTER);
            layoutVertical.addView(layoutHorizontalPassword);

            passwordView = new TextView(view.getContext());
            passwordView.setText("Password: " + this.password);
            passwordView.setGravity(Gravity.START);
            passwordView.setGravity(Gravity.CENTER);
            layoutHorizontalPassword.addView(passwordView);

            passwordCopy = new Button(view.getContext());
            passwordCopy.setText("Copy to Clipboard");
            passwordCopy.setGravity(Gravity.END);
            passwordCopy.setBackgroundColor(Color.GRAY);
            passwordCopy.setOnClickListener(v ->{
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("username", this.password);
                clipboard.setPrimaryClip(clip);
            });
            layoutHorizontalPassword.addView(passwordCopy);
        }

        public CardView getCard() {
            return this.card;
        }
    }
     */
}



