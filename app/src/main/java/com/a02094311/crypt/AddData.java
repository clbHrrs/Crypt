package com.a02094311.crypt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.a02094311.crypt.databinding.FragmentAddDataBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddData extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentAddDataBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String masterPassword;

    public AddData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddData.
     */
    // TODO: Rename and change types and number of parameters
    public static AddData newInstance(String param1, String param2) {
        AddData fragment = new AddData();
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

        binding.generatePassword.setOnClickListener(view ->{
            Random rand = new Random();
            String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#";
            StringBuilder sb = new StringBuilder(15);
            for (int i = 0; i < 15; i++) {
                sb.append(DATA.charAt(rand.nextInt(DATA.length())));
            }
            binding.addDataPassword.setText(sb.toString());
        });

        binding.addDataSubmit.setOnClickListener(view -> {
            String title = binding.addDataTitle.getText().toString();
            String username = binding.addDataUsername.getText().toString();
            String password = binding.addDataPassword.getText().toString();

            if (username.length() <= 0 || password.length() <= 0) {
                binding.addDataError.setText("Please fill in all fields.");
                binding.addDataError.setVisibility(View.VISIBLE);
            }

            else {
                try {
                    FirebaseUser user = mAuth.getCurrentUser();
                    SecureRandom random = new SecureRandom();
                    byte[] saltBytesTitle = new byte[8];
                    byte[] saltBytesPassword = new byte[8];
                    byte[] saltBytesUsername = new byte[8];
                    random.nextBytes(saltBytesTitle);
                    random.nextBytes(saltBytesPassword);
                    random.nextBytes(saltBytesUsername);
                    String saltTitle = Base64.encodeToString(saltBytesTitle, Base64.DEFAULT);
                    String saltPassword = Base64.encodeToString(saltBytesPassword, Base64.DEFAULT);
                    String saltUsername = Base64.encodeToString(saltBytesUsername, Base64.DEFAULT);

                    KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), saltBytesTitle, 65536, 128);
                    KeySpec spec1 = new PBEKeySpec(masterPassword.toCharArray(), saltBytesPassword, 65536, 128);
                    KeySpec spec2 = new PBEKeySpec(masterPassword.toCharArray(), saltBytesUsername, 65536, 128);
                    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    byte[] keyBytes = f.generateSecret(spec).getEncoded();
                    byte[] keyBytes1 = f.generateSecret(spec1).getEncoded();
                    byte[] keyBytes2 = f.generateSecret(spec2).getEncoded();
                    SecretKeySpec secret = new SecretKeySpec(keyBytes, "AES");
                    SecretKeySpec secret1 = new SecretKeySpec(keyBytes1, "AES");
                    SecretKeySpec secret2 = new SecretKeySpec(keyBytes2, "AES");
                    Cipher cipher = null;
                    Cipher cipher1 = null;
                    Cipher cipher2 = null;
                    cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secret);
                    cipher1.init(Cipher.ENCRYPT_MODE, secret1);
                    cipher2.init(Cipher.ENCRYPT_MODE, secret2);

                    byte[] cipherTextTitle = cipher.doFinal(title.getBytes("UTF-8"));
                    byte[] cipherTextUsername = cipher1.doFinal(username.getBytes("UTF-8"));
                    byte[] cipherTextPassword = cipher2.doFinal(password.getBytes("UTF-8"));

                    String cipherTextTitleString = Base64.encodeToString(cipherTextTitle, Base64.DEFAULT);
                    String cipherTextUsernameString = Base64.encodeToString(cipherTextUsername, Base64.DEFAULT);
                    String cipherTextPasswordString = Base64.encodeToString(cipherTextPassword, Base64.DEFAULT);


                    Map<String, String> data = new HashMap<>();
                    data.put("title", cipherTextTitleString);
                    data.put("username", cipherTextUsernameString);
                    data.put("password", cipherTextPasswordString);
                    data.put("titleSalt", saltTitle);
                    data.put("passwordSalt", saltPassword);
                    data.put("usernameSalt", saltUsername);
                    db.collection(user.getUid()).
                            add(data);
                    Navigation.findNavController(binding.getRoot()).
                            navigate((AddDataDirections.
                                    actionAddDataToVaultFragment()).setPassword(masterPassword));
                } catch (Exception e) {
                    Log.e("exception", e.toString());
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddDataBinding.inflate(getLayoutInflater());
        masterPassword = AddDataArgs.fromBundle(getArguments()).getPassword();
        return binding.getRoot();
    }
}