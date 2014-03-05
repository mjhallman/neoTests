
import java.util.List;

/**
 * User: Matt Hallman
 * Date: 2/27/14
 * Time: 10:40 AM
 */
public interface MockAPI {

    public class Edit {
        int userId;
        int docId;
    }

    public class CreateDoc {
        int userId;
        int docId;
        long ts;
    }

    public class Approval {
        int reviewId; // The reviewId for the approval.
        int userId; // User doing the approving.
        int docId; // Doc being approved.
        long ts; // ts when approval occured.

    }

    void createUser(int id, String username);

    void createDoc(long ts, int userId, int docId, String docType);

    void editDoc(long ts, int userId, int docId);

    void bulkCreate(List<CreateDoc> creates);

    void createReference(int id, int references, String referenceType);

    void createReviewRequest(long ts, int reviewId, int userId, int docToReview, List<Integer> reviewers);

    void approveDocument(Approval approval);

    // Who hasn’t completed review for request X?
    List<Integer> getReviewersNotCompleted(int reviewRequest);

    // What documents are left to be approved in review request X?
    List<Integer> getDocsNotApprovedForRequest(int reviewRequest);

    // Who approved this document and when?
    List<Approval> getApprovalsForDoc(int docId);

    // What documents in DITA Map Y are left to be approved by User X?
    List<Integer> getDocsNotYetApprovedByUser(int user, int reviewRequest);

    // Get the history of events. return type TBD... just print out for now.
    void printHistoryForDoc(int doc);

    int getExplicitDependencies(int doc);

    int getTotalDependencies(int doc);
}
