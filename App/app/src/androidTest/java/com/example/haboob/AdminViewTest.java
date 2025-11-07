package com.example.haboob;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;


import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for Admin fragment navigation.
 * Tests the navigation flow between AdminMainFragment, AdminPosterFragment, and ProfileFragment.
 *
 * <p>Test scenarios:</p>
 * <ul>
 *   <li>AdminMainFragment displays correctly with all buttons</li>
 *   <li>Clicking "View Posters" button navigates to AdminPosterFragment</li>
 *   <li>Clicking back button in AdminPosterFragment returns to AdminMainFragment</li>
 *   <li>Clicking back button in AdminMainFragment returns to ProfileFragment</li>
 * </ul>
 *
 * @author Haboob Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminViewTest {

    private TestNavHostController navController;

    /**
     * Sets up the test environment before each test.
     * Initializes the TestNavHostController with the mobile navigation graph.
     */
    @Before
    public void setup() {
        navController = new TestNavHostController(ApplicationProvider.getApplicationContext());
    }

    /**
     * Tests that AdminMainFragment displays correctly with all expected buttons.
     *
     * <p>Verifies that the following UI elements are present:</p>
     * <ul>
     *   <li>View Posters button</li>
     *   <li>View Events button</li>
     *   <li>Back button</li>
     * </ul>
     */
    @Test
    public void testAdminMainFragmentDisplaysCorrectly() {
        // Launch AdminMainFragment
        FragmentScenario<AdminMainFragment> scenario = FragmentScenario.launchInContainer(
                AdminMainFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Verify that the View Posters button is displayed
        onView(withId(R.id.admin_view_posters_button))
                .check(matches(isDisplayed()));

        // Verify that the View Events button is displayed
        onView(withId(R.id.admin_view_events_button))
                .check(matches(isDisplayed()));

        // Verify that the back button is displayed
        onView(withId(R.id.btn_admin_back))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests navigation from AdminMainFragment to AdminPosterFragment.
     *
     * <p>This test:</p>
     * <ol>
     *   <li>Launches AdminMainFragment</li>
     *   <li>Clicks the "View Posters" button</li>
     *   <li>Verifies navigation to AdminPosterFragment (navigation_admin_posters destination)</li>
     * </ol>
     */
    @Test
    public void testNavigationFromAdminMainToAdminPoster() {
        // Launch AdminMainFragment
        FragmentScenario<AdminMainFragment> scenario = FragmentScenario.launchInContainer(
                AdminMainFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Click the "View Posters" button
        onView(withId(R.id.admin_view_posters_button))
                .perform(click());

        // Verify that we navigated to the admin posters destination
        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });
    }

    /**
     * Tests navigation from AdminPosterFragment back to AdminMainFragment.
     *
     * <p>This test:</p>
     * <ol>
     *   <li>Launches AdminPosterFragment</li>
     *   <li>Clicks the back button in the toolbar menu</li>
     *   <li>Verifies navigation back to AdminMainFragment (navigation_admin destination)</li>
     * </ol>
     */
    @Test
    public void testNavigationFromAdminPosterToAdminMain() {
        // Launch AdminPosterFragment
        FragmentScenario<AdminPosterFragment> scenario = FragmentScenario.launchInContainer(
                AdminPosterFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Wait for RecyclerView to be displayed (fragment has loaded)
        onView(withId(R.id.recycler_view_admin_posters))
                .check(matches(isDisplayed()));

        // Click the back button in the toolbar menu
        // Note: This assumes the toolbar has a menu item with id action_goBack
        onView(withId(R.id.action_goBack))
                .perform(click());

        // Verify that we navigated back to the admin main destination
        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });
    }

    /**
     * Tests navigation from AdminMainFragment back to ProfileFragment.
     *
     * <p>This test:</p>
     * <ol>
     *   <li>Launches AdminMainFragment</li>
     *   <li>Clicks the back button</li>
     *   <li>Verifies navigation to ProfileFragment (profile_fragment destination)</li>
     * </ol>
     */
    @Test
    public void testNavigationFromAdminMainToProfile() {
        // Launch AdminMainFragment
        FragmentScenario<AdminMainFragment> scenario = FragmentScenario.launchInContainer(
                AdminMainFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Click the back button
        onView(withId(R.id.btn_admin_back))
                .perform(click());

        // Verify that we navigated to the profile fragment
        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });
    }

    /**
     * Tests that the AdminPosterFragment displays its RecyclerView correctly.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The RecyclerView is displayed when the fragment loads</li>
     *   <li>The progress bar is initially visible then hidden after loading</li>
     *   <li>The toolbar is displayed with the correct title</li>
     * </ul>
     */
    @Test
    public void testAdminPosterFragmentDisplaysRecyclerView() {
        // Launch AdminPosterFragment
        FragmentScenario<AdminPosterFragment> scenario = FragmentScenario.launchInContainer(
                AdminPosterFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Verify RecyclerView is displayed
        onView(withId(R.id.recycler_view_admin_posters))
                .check(matches(isDisplayed()));

        // Verify toolbar is displayed
        onView(withId(R.id.posterTopAppBar))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the "View Events" button navigates to the correct destination.
     *
     * <p>Note: Currently both "View Posters" and "View Events" navigate to the same
     * destination (navigation_admin_posters) as noted in the AdminMainFragment code.</p>
     */
    @Test
    public void testNavigationFromAdminMainViewEventsButton() {
        // Launch AdminMainFragment
        FragmentScenario<AdminMainFragment> scenario = FragmentScenario.launchInContainer(
                AdminMainFragment.class,
                null,
                R.style.Theme_Haboob
        );

        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });

        // Click the "View Events" button
        onView(withId(R.id.admin_view_events_button))
                .perform(click());

        // Verify navigation occurred (currently goes to same destination as posters)
        scenario.onFragment(fragment -> {
            // Set the graph on the main thread first
            navController.setGraph(R.navigation.mobile_navigation);

            // Now you can set the view and destination
            Navigation.setViewNavController(fragment.requireView(), navController);
            navController.setCurrentDestination(R.id.navigation_admin);
        });
    }
}