
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: Matt Hallman
 * Date: 2/27/14
 * Time: 10:45 AM
 */
public class APIRunner {

//    static final int NUM_USERS = 3;
//    static final int NUM_DOCS = 3;


    static MockAPI api;

    static Random r = new Random();


    public static void largeRelationshipsDemo() {
        int numUsers = 10;
        int numDocs = 5;

        System.out.println("Creating " + numUsers + " users.");
        createUsers(numUsers);


        // Create docs:
//        System.out.println("Creating " + numDocs + " docs.");
//        for (int i = 0; i < numDocs; i++) {
//            api.createDoc(getTS(), r.nextInt(numUsers), i, getRandomDocType());
//        }

        // Create docs while building a large tree.
        System.out.println("Creating tree.");
        int NUM_CHILDREN = 8;
        int NUM_NODES_WITH_CHILDREN = 10;

        int docId = 0;
        int treeId = 0;
        api.createDoc(getTS(), 0, docId++, getRandomDocType());
        for (int i = 0; i < NUM_NODES_WITH_CHILDREN; i++) {
            for (int j = 0; j < NUM_CHILDREN; j++) {
                api.createDoc(getTS(), 0, docId, getRandomDocType());
                api.createReference(treeId, docId++, getReferenceType());
            }
            treeId++;
        }


        // Create random relationships
        int numRelationships = 10;
        System.out.println("Creating " + numRelationships + " relationships.");
//
        for (int i = 0; i < numRelationships; i++) {
            int referencer = r.nextInt(numDocs);
            int referenced = r.nextInt(numDocs);
            while (referencer == referenced) referenced = r.nextInt(numDocs);

            api.createReference(referencer, referenced, getReferenceType());

        }


        // Create a review request
        int numReviewers = 5;

        int requesterUserId = r.nextInt(numUsers);
        int docToReview = r.nextInt(numDocs);
        int REQUEST_ID = 5;
        List<Integer> reviewerIds = new ArrayList<>();
        for (int i = 0; i < numReviewers; i++) {
            int reviewerId = r.nextInt(numUsers);
            while (reviewerId == requesterUserId || reviewerIds.contains(reviewerId)) reviewerId = r.nextInt(numUsers);
            reviewerIds.add(reviewerId);
        }

        System.out.println("Creating review request with " + numReviewers + " reviewers.");
        api.createReviewRequest(getTS(), REQUEST_ID, requesterUserId, docToReview, reviewerIds);


        System.out.println("done");

        int numReviewersToComplete = 2;

        System.out.println("Completing approval for " + numReviewersToComplete + " reviewers.");
        System.out.println("Total Reviewers: " + reviewerIds);
        for (int i = 0; i < numReviewersToComplete; i++) {
            int index = r.nextInt(reviewerIds.size());
            int userId = reviewerIds.get(index);
            reviewerIds.remove(index);
            System.out.println("Completing approvals for reviewer: " + userId);
            List<Integer> docsToApprove = api.getDocsNotYetApprovedByUser(userId, REQUEST_ID);
            if (r.nextBoolean())
                for (int k = 0; k < 5; k++) docsToApprove.remove(0);
//            start = System.nanoTime();
            for (Integer doc : docsToApprove) {
                final MockAPI.Approval approval = new MockAPI.Approval();
                approval.docId = doc;
                approval.ts = getTS();
                approval.reviewId = REQUEST_ID;
                approval.userId = userId;
                api.approveDocument(approval);
            }
        }


        List<Integer> docsNotApproved = api.getDocsNotApprovedForRequest(5);
        System.out.println("docs not approved: " + docsNotApproved);







    }


    public static String getReferenceType() {
        ArrayList<String> referenceTypes = new ArrayList<>();
        referenceTypes.add("conref");
        referenceTypes.add("keyref");
        return referenceTypes.get(r.nextInt(referenceTypes.size()));
    }



//    public static void largeMapDemo() {
//        int numUsers = 5;
//        int numDocs = 10;
//        System.out.println("Creating " + numUsers + " users");
//        createUsers(numUsers);
////        System.out.println("Creating " + numDocs + " docs");
////        createDocs(numDocs);
//
//        // Create tree where every node has 5 sibling
//        int NUM_SIBLINGS = 5;
//        int NUM_DOCS_WITH_SIBLINGS = 2000;
//
//        int docID = 0;
//
//        System.out.println("Creating tree");
//        // root node:
//        api.createDoc(getTS(), 1, docID++, getRandomDocType());
//
//        for (int doc = 0; doc < NUM_DOCS_WITH_SIBLINGS; doc++) {
//            // Create child docs
//            final List<Integer> childDocIds = new ArrayList<>();
//            for (int sibling = 0; sibling < NUM_SIBLINGS; sibling++) {
//                final int newDoc = docID++;
//                childDocIds.add(newDoc);
//                api.createDoc(getTS(), 1, newDoc, getRandomDocType());
//
//
//            }
//            // Make them children:
//            final int finalDoc = doc;
////            api.createReference(getTS(), finalDoc, childDocIds);
//        }
//
//
//
//        System.out.println("Creating review request on root");
//        int NUM_REVIEWERS = 20;
//        List<Integer> reviewers = new ArrayList<>();
//        for (int i = 1; i < NUM_REVIEWERS; i++) {
//            reviewers.add(i);
//        }
//        api.createReviewRequest(getTS(), 1, 1, 0, reviewers);
//
//        List<Integer> docsNotApproved = api.getDocsNotApprovedForRequest(1);
//        System.out.println(" --> docs (" + docsNotApproved.size() +  "): ");
//
//        System.out.println("Who hasn’t completed review for request 1?");
//        long start = System.nanoTime();
//        reviewers = api.getReviewersNotCompleted(1);
//        long end = System.nanoTime();
//        Double executionTime = (double) (end - start) / 1000000000;
//        System.out.println(" --> time: " + executionTime + " reviewers: " + reviewers);
//
//        System.out.println("Completing approval for 10 users");
//        for (int i = 0; i < 10; i++) {
//            int userId = r.nextInt(NUM_REVIEWERS);
//            List<Integer> docsToApprove = api.getDocsNotYetApprovedByUser(userId, 1);
//            start = System.nanoTime();
//            for (Integer doc : docsToApprove) {
//                final MockAPI.Approval approval = new MockAPI.Approval();
//                approval.docId = doc;
//                approval.ts = getTS();
//                approval.reviewId = 1;
//                approval.userId = userId;
//                api.approveDocument(approval);
//
//            }
//            end = System.nanoTime();
//            executionTime = (double) (end - start) / 1000000000;
//
//            System.out.println("Time to approve " + docsToApprove.size() + " docs: " + executionTime);
//
//        }
//
//
//
//
//        System.out.println("Who hasn’t completed review for request 1?");
//        start = System.nanoTime();
//        reviewers = api.getReviewersNotCompleted(1);
//        end = System.nanoTime();
//        executionTime = (double) (end - start) / 1000000000;
//
//        System.out.println(" --> time: " + executionTime + " reviewers: " + reviewers);
//
//        System.out.println("What documents are left to be approved in review request 1?");
//
//        start = System.nanoTime();
//        docsNotApproved = api.getDocsNotApprovedForRequest(1);
//        end = System.nanoTime();
//        executionTime = (double) (end - start) / 1000000000;
//        System.out.println(" --> time: " + executionTime + " docs(" + docsNotApproved.size() +  "): ");
//    }




    public static void main(String... args) {

        MockNeo4jAPI  mockNeo4jAPI = new MockNeo4jAPI();
        mockNeo4jAPI.clearDB();

        api = mockNeo4jAPI;

        api = new MockNeo4jAPI();

        largeRelationshipsDemo();

//        doSimpleDemo();


    }

    private static void createReviewRequest(int reviewId) {
        int docToReview = 0; // The map
        List<Integer> reviewers = new ArrayList<>();
        reviewers.add(2);
        reviewers.add(3);
        api.createReviewRequest(getTS(), reviewId, 1, docToReview, reviewers);
        System.out.println(String.format(" --> User %d creates a review request for %d (a map) for reviewers %s", 1, docToReview, reviewers));

    }

    public static void createUsers(int numUsers) {
        for (int i = 0; i < numUsers; i++) {
            api.createUser(i, "user" + i);
        }
    }

    public static void createDocs(int numDocs) {
        for (int i = 0; i < numDocs; i++) {
            api.createDoc(getTS(), 1, i, getRandomDocType());
        }
    }

    public static void createReferences() {
        List<Integer> references = new ArrayList<Integer>();
        references.add(1);
        references.add(2);
//        api.createReference(getTS(), 0, references );
    }


    public static long getTS() {
        return System.nanoTime();
    }

    public static String getRandomDocType() {
        ArrayList<String> docTypes = new ArrayList<>();
        docTypes.add("Task");
        docTypes.add("Concept");
        docTypes.add("Reference");
        int index = r.nextInt(docTypes.size());
        return docTypes.get(index);

    }



    public static void doSimpleDemo() {

        int NUM_DOCS = 4;
        int NUM_USERS = 4;


        System.out.println("Creating" + NUM_USERS + " users");
        createUsers(NUM_USERS);
        System.out.println("Creating" + NUM_DOCS + " docs");
        createDocs(NUM_DOCS);

        int NUM_EDITS = 0;
        System.out.println("Performing " + NUM_EDITS + " random edits");
        for (int i=0; i < NUM_EDITS; i++) {
            int user = r.nextInt(NUM_USERS);
            int doc = r.nextInt(NUM_DOCS);
            api.editDoc(getTS(), user, doc);
        }




        createReferences();

        int reviewId = 5;
//        System.out.println("Create review request with id:" + reviewId);
        createReviewRequest(reviewId);

        System.out.println("Who hasn't completed review for request 5?");
        System.out.println(" --> reviewers: " + api.getReviewersNotCompleted(5));

        System.out.println("User 2 approves document 1 for review request 5");
        MockAPI.Approval approval = new MockAPI.Approval();
        approval.docId = 1;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 2;
        api.approveDocument(approval);

        System.out.println("Who hasn’t completed review for request 5?");
        System.out.println(" --> reviewers: " + api.getReviewersNotCompleted(5));

        System.out.println("User 2 approves document 2 for review request 5");
        approval = new MockAPI.Approval();
        approval.docId = 2;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 2;
        api.approveDocument(approval);

        System.out.println("Who hasn’t completed review for request 5?");
        System.out.println(" --> reviewers: " + api.getReviewersNotCompleted(5));

        System.out.println("What documents are left to be approved in review request 5?");
        System.out.println(" --> docs: " + api.getDocsNotApprovedForRequest(5));

        System.out.println("User 3 approves document 2 for review request 5");
        approval = new MockAPI.Approval();
        approval.docId = 2;
        approval.reviewId = 5;
        approval.ts = getTS();
        approval.userId = 3;
        api.approveDocument(approval);

        System.out.println("What documents are left to be approved in review request 5?");
        System.out.println(" --> docs: " + api.getDocsNotApprovedForRequest(5));

    }


}
