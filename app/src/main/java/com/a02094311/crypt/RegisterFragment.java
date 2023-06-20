package com.a02094311.crypt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a02094311.crypt.databinding.FragmentRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Context;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
    }

    @Override
    public void onStart() {
        super.onStart();

        binding.signupButton.setOnClickListener(view -> {
            String email = binding.registerEmail.getText().toString();
            String password1 = binding.registerPassword.getText().toString();
            String password2 = binding.registerConfirmPassword.getText().toString();

            if (email.length() <= 0 || password1.length() <= 0 || password2.length() <= 0) {
                binding.registerError.setText("Please fill in all fields.");
                binding.registerError.setVisibility(View.VISIBLE);
            }

            else if (password1.length() < 10 || password2.length() < 10) {
                binding.registerError.setText("Your password must be at least 10 characters long");
                binding.registerError.setVisibility(View.VISIBLE);
            }

            else if (!password1.equals(password2)) {
                binding.registerError.setText("Passwords must match.");
                binding.registerError.setVisibility(View.VISIBLE);
            }

            else {
                mAuth.createUserWithEmailAndPassword(email, password1)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Map<String, String> userMap = new HashMap<>();
                                    userMap.put("premium", "true");
                                    db.collection(user.getUid()).
                                            document("User").
                                            set(userMap);
                                    Navigation.findNavController(binding.getRoot()).
                                            navigate((RegisterFragmentDirections.
                                                    actionRegisterFragmentToVaultFragment()).
                                                    setPassword(password1));
                                } else {
                                    binding.registerError.setText("Signup error, please try again later");
                                    binding.registerError.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}