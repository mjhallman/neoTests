

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * User: Matt Hallman
 * Date: 3/4/14
 * Time: 11:29 AM
 */
public class APITest {


    int NUM_USERS = 4;
    int NUM_DOCS = 5;


    @Test
    public void testWorkflow() {

        MockNeo4jAPI  api = new MockNeo4jAPI();
        api.clearDB();

        for (int i = 0; i < NUM_USERS; i++)
            api.createUser(i, "user" + i);
        for (int i=0; i< NUM_DOCS; i++)
            api.createDoc(getTS(), 0, i, "Concept");

        // Create relationships
        api.createReference(0, 1, "conref");
        api.createReference(0, 2, "conref");
        api.createReference(1, 3, "keyref");


        // Test graph explicit references
        Assert.assertEquals(2, api.getExplicitDependencies(0));
        Assert.assertEquals(1, api.getExplicitDependencies(1));
        Assert.assertEquals(0, api.getExplicitDependencies(2));
        Assert.assertEquals(0, api.getExplicitDependencies(3));
        Assert.assertEquals(0, api.getExplicitDependencies(4));

        // Test graph implicit references
        Assert.assertEquals(3, api.getTotalDependencies(0));
        Assert.assertEquals(1, api.getTotalDependencies(1));
        Assert.assertEquals(0, api.getTotalDependencies(2));
        Assert.assertEquals(0, api.getTotalDependencies(3));
        Assert.assertEquals(0, api.getTotalDependencies(4));








        List<Integer> reviewer = new ArrayList<>();
        reviewer.add(2);
        reviewer.add(3);
        api.createReviewRequest(getTS(), 5, 1, 0, reviewer);

        List<Integer> docsNotApproved = new ArrayList<>();
        docsNotApproved.add(1);
        docsNotApproved.add(2);
        docsNotApproved.add(3);

        List<Integer> docsNotApprovedFromAPI = api.getDocsNotApprovedForRequest(5);
        List<Integer> docsNotApprovedReviewer2FromApi = api.getDocsNotYetApprovedByUser(2, 5);
        List<Integer> docsNotApprovedReviewer3FromApi = api.getDocsNotYetApprovedByUser(3, 5);

        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedFromAPI));
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer2FromApi));
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));


        ArrayList<Integer> reviewersWhoHaventCompleted = new ArrayList<>();
        reviewersWhoHaventCompleted.add(2);
        reviewersWhoHaventCompleted.add(3);
        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));



        // Perform approvals

        // User 2 approves doc 2;
        MockAPI.Approval approval = new MockAPI.Approval();
        approval.userId = 2;
        approval.docId = 2;
        approval.ts = getTS();
        approval.reviewId = 5;
        api.approveDocument(approval);

        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));



        docsNotApproved = new ArrayList<>();
        docsNotApproved.add(1);
        docsNotApproved.add(3);

        docsNotApprovedReviewer2FromApi = api.getDocsNotYetApprovedByUser(2, 5);

        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer2FromApi));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));


        // User 2 approves doc 1.
        approval.docId = 1;
        approval.ts = getTS();
        api.approveDocument(approval);

        docsNotApproved = new ArrayList<>();
        docsNotApproved.add(3);
        docsNotApprovedReviewer2FromApi = api.getDocsNotYetApprovedByUser(2, 5);

        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer2FromApi));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));


        // User 2 approves doc 3.
        approval.docId = 3;
        api.approveDocument(approval);

        docsNotApproved = new ArrayList<>();
        docsNotApprovedReviewer2FromApi = api.getDocsNotYetApprovedByUser(2, 5);

        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer2FromApi));

        reviewersWhoHaventCompleted = new ArrayList<>();
        reviewersWhoHaventCompleted.add(3);
        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));



        docsNotApproved = new ArrayList<>();
        docsNotApproved.add(1);
        docsNotApproved.add(2);
        docsNotApproved.add(3);
        docsNotApprovedReviewer3FromApi = api.getDocsNotYetApprovedByUser(3, 5);
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));




        // User 2 has completed their approvals.

        // User 3 Approves doc 1.
        approval = new MockAPI.Approval();
        approval.docId = 1;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 3;
        api.approveDocument(approval);
        docsNotApproved = new ArrayList<>();
        docsNotApproved.add(2);
        docsNotApproved.add(3);
        docsNotApprovedReviewer3FromApi = api.getDocsNotYetApprovedByUser(3, 5);
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));


        // Doc 1 has now passed approval.

        List<Integer> docsNeedingAprovalFromAPI = api.getDocsNotApprovedForRequest(5);
        List<Integer> docsNeedingApproval = new ArrayList<>();
        docsNeedingApproval.add(2);
        docsNeedingApproval.add(3);
        Assert.assertEquals(new HashSet<>(docsNeedingApproval), new HashSet<>(docsNeedingAprovalFromAPI));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));


        // User 3 Approves doc 2.
        approval = new MockAPI.Approval();
        approval.docId = 2;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 3;
        api.approveDocument(approval);
        docsNotApproved = new ArrayList<>();
        docsNotApproved.add(3);
        docsNotApprovedReviewer3FromApi = api.getDocsNotYetApprovedByUser(3, 5);
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));

        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));



        // User 3 Approves doc 3.
        approval = new MockAPI.Approval();
        approval.docId = 3;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 3;
        api.approveDocument(approval);
        docsNotApproved = new ArrayList<>();
        docsNotApprovedReviewer3FromApi = api.getDocsNotYetApprovedByUser(3, 5);
        Assert.assertEquals(new HashSet<>(docsNotApproved), new HashSet<>(docsNotApprovedReviewer3FromApi));

        reviewersWhoHaventCompleted = new ArrayList<>();
        Assert.assertEquals(new HashSet<>(reviewersWhoHaventCompleted), new HashSet<>(api.getReviewersNotCompleted(5)));



        // All docs have passed approval:

        Assert.assertEquals(new ArrayList<Integer>(), api.getDocsNotApprovedForRequest(5));


    }

    public static long getTS() {
        return System.nanoTime();
    }



}
