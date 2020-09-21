package teammates.test.cases.webapi;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;
import teammates.common.util.Const.ParamsNames;
import teammates.common.util.EmailType;
import teammates.common.util.EmailWrapper;
import teammates.ui.webapi.StudentCourseJoinEmailWorkerAction;

/**
 * SUT: {@link StudentCourseJoinEmailWorkerAction}.
 */
public class StudentCourseJoinEmailWorkerActionTest extends BaseActionTest<StudentCourseJoinEmailWorkerAction> {

    @Override
    protected String getActionUri() {
        return Const.TaskQueue.STUDENT_COURSE_JOIN_EMAIL_WORKER_URL;
    }

    @Override
    protected String getRequestMethod() {
        return POST;
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        verifyOnlyAdminCanAccess();
    }

    @Override
    @Test
    public void testExecute() {

        CourseAttributes course1 = typicalBundle.courses.get("typicalCourse1");
        StudentAttributes stu1InCourse1 = typicalBundle.students.get("student1InCourse1");

        ______TS("typical case: new student joining");

        String[] submissionParams = new String[] {
                ParamsNames.COURSE_ID, course1.getId(),
                ParamsNames.STUDENT_EMAIL, stu1InCourse1.email,
                ParamsNames.IS_STUDENT_REJOINING, "false",
        };

        StudentCourseJoinEmailWorkerAction action = getAction(submissionParams);
        action.execute();

        verifyNumberOfEmailsSent(action, 1);

        EmailWrapper email = action.getEmailSender().getEmailsSent().get(0);
        assertEquals(String.format(EmailType.STUDENT_COURSE_JOIN.getSubject(), course1.getName(),
                                   course1.getId()),
                     email.getSubject());
        assertEquals(stu1InCourse1.email, email.getRecipient());

        ______TS("typical case: old student rejoining (after google id reset)");

        submissionParams = new String[] {
                ParamsNames.COURSE_ID, course1.getId(),
                ParamsNames.STUDENT_EMAIL, stu1InCourse1.email,
                ParamsNames.IS_STUDENT_REJOINING, "true",
        };

        action = getAction(submissionParams);
        action.execute();

        verifyNumberOfEmailsSent(action, 1);

        email = action.getEmailSender().getEmailsSent().get(0);
        assertEquals(String.format(EmailType.STUDENT_COURSE_REJOIN_AFTER_GOOGLE_ID_RESET.getSubject(),
                                   course1.getName(), course1.getId()),
                     email.getSubject());
        assertEquals(stu1InCourse1.email, email.getRecipient());
    }

}