package com.a02094311.crypt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a02094311.crypt.databinding.FragmentChangePasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.installations.remote.TokenResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangePasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentChangePasswordBinding binding;
    private FirebaseAuth mAuth;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangePasswordFragment newInstance(String param1, String param2) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
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

        binding.changePasswordButton.setOnClickListener(view -> {
            String password1 = binding.changePasswordPassword.getText().toString();
            String password2 = binding.changePasswordConfirmPassword.getText().toString();

            if (password1.length() <= 0 || password2.length() <= 0) {
                binding.changePasswordError.setText("Please fill in all fields.");
                binding.changePasswordError.setVisibility(View.VISIBLE);
            }

            else if (password1.length() < 10 || password2.length() < 10) {
                binding.changePasswordError.setText("Your password must be at least 10 characters long");
                binding.changePasswordError.setVisibility(View.VISIBLE);
            }

            else if (!password1.equals(password2)) {
                binding.changePasswordError.setText("Passwords must match.");
                binding.changePasswordError.setVisibility(View.VISIBLE);
            }

            else {
                FirebaseUser user = mAuth.getCurrentUser();

                user.updatePassword(password1).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.signOut();
                            Navigation.findNavController(binding.getRoot()).
                                    navigate(ChangePasswordFragmentDirections.
                                            actionChangePasswordFragmentToLoginFragment());
                        } else {
                            binding.changePasswordError.setText("Unexpected error, please try again later");
                            binding.changePasswordError.setVisibility(View.VISIBLE);
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
        binding = FragmentChangePasswordBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}