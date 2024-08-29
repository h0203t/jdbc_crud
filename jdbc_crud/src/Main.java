import db.Boards;
import db.Users;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static SHA256 sha256 = new SHA256();

    private static List<Boards> boards = new ArrayList<>();
    private static List<Users> users = new ArrayList<>();
    public static Scanner sc = new Scanner(System.in);

    private static Connection conn = null;
    //게시판
    static String title, content = null;
    static int boardId;
    //회원
    static String userId, password, tel = null;
    static String idCheck, pwCheck = null;

    static String cryptogram = null;

    public static Connection dbConn() {
        if (conn == null) {
            try {

                Class.forName("oracle.jdbc.OracleDriver");

                conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.28:1521/xe", "javadb", "1234");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    public static void dbExit() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void boardInsert() {
        String sql = "INSERT INTO BOARDS(board_id, title, content, creation_date, user_id) " +
                "VALUES (seq_board_id.nextval, ?, ?, sysdate, ?)";

        try (PreparedStatement pstmt = dbConn().prepareStatement(sql, new String[]{"board_id"})) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, idCheck);
            pstmt.executeUpdate();
            System.out.println("게시글이 성공적으로 작성되었습니다.");
        } catch (SQLException e) {
            System.out.println("로그인이 되어 있지않습니다.");
//            e.printStackTrace();
        }
    }

    public static void join() {
        String sql = "INSERT INTO USERS(user_id, password, tel)" +
                "VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = dbConn().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            cryptogram = sha256.encrypt(password);
            pstmt.setString(2, cryptogram);
            pstmt.setString(3, tel);
            pstmt.executeUpdate();
            System.out.println("회원가입이 성공적으로 완료되었습니다.");
        } catch (SQLException | NoSuchAlgorithmException e) {
//            System.out.println("가입 실패! 중복 된 아이디 입니다.");
            e.printStackTrace();
        }
    }

    public static boolean login(String idCheck, String password) {
        String sql = "SELECT password FROM USERS WHERE user_id = ?";
        try (Connection conn = dbConn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, idCheck);

             ResultSet rs = pstmt.executeQuery();
             if (rs.next()) {

                 String dbPassword = rs.getString("password");

                 String encryptedPassword = sha256.encrypt(password);

                 if (dbPassword.equals(encryptedPassword)) {
                    return true;
                 }
             }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false; // 로그인 실패
    }

    public static void selectBoards() {
        String sql = "SELECT board_id, title, content, creation_date, user_id FROM BOARDS";

        try (PreparedStatement pstmt = dbConn().prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Boards boards = new Boards();
                boards.setBoardId(rs.getInt("board_id"));
                boards.setTitle(rs.getString("title"));
                boards.setContent(rs.getString("content"));
                boards.setDate(rs.getDate("creation_date"));
                boards.setUserId(rs.getString("user_id"));


                System.out.println("---------------------------------------------------------------------------");
                System.out.println("NO: " + boards.getBoardId() + " | 제목: " + boards.getTitle() + " | 내용: " + boards.getContent() + " | 작성날짜: " + boards.getDate() + " | 작성자: " + boards.getUserId());
                System.out.println("---------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("게시물 작성에 실패하였습니다.");
//            e.printStackTrace();
        }
    }

    public static void updateBoards() {
        String sql = "UPDATE BOARDS SET TITLE = ?, CONTENT = ? WHERE BOARD_ID = ? AND USER_ID = ?";

        try (PreparedStatement pstmt = dbConn().prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, boardId);
            pstmt.setString(4, idCheck);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("수정 완료");
            } else {
                System.out.println("수정 실패! 작성한 사용자가 아닙니다.");
            }
        } catch (SQLException e) {
            System.out.println("게시글 수정에 실패하였습니다.");
//            e.printStackTrace(v);
        }
    }

    public static void deleteBoards() {
        String sql = "DELETE FROM BOARDS WHERE BOARD_ID = ? AND USER_ID = ?";

        try (PreparedStatement pstmt = dbConn().prepareStatement(sql)) {

            pstmt.setInt(1, boardId);
            pstmt.setString(2, idCheck);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("삭제 완료");
            } else {
                System.out.println("삭제 실패! 작성한 사용자가 아닙니다.");
            }
        } catch (SQLException e) {
//            System.out.println("게시글 수정에 실패하였습니다.");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("--------------------------");
            System.out.println("1.게시판 |  2.회원  | 3.종료");
            System.out.println("--------------------------");
            System.out.print("메뉴 선택> ");

            int selectNum = sc.nextInt();
            sc.nextLine();

            if (selectNum == 1) {
                System.out.println("---------------------------게시판 메뉴------------------------------");
                System.out.println("1.게시글 작성 | 2.게시글 조회 | 3.게시글 수정 | 4.게시글 삭제 | 5.이전메뉴");
                System.out.println("------------------------------------------------------------------");
                System.out.print("메뉴 선택> ");

                int boardNum = sc.nextInt();
                sc.nextLine();

                if (boardNum == 1) {
                    System.out.println("게시글 작성");
                    System.out.print("제목 : ");
                    title = sc.nextLine();
                    System.out.print("내용 : ");
                    content = sc.nextLine();
                    boardInsert();
                } else if (boardNum == 2) {
                    selectBoards();
                } else if (boardNum == 3) {
                    System.out.print("수정할 게시글 번호 : ");
                    boardId = sc.nextInt();
                    System.out.print("수정할 제목 :");
                    title = sc.next();
                    System.out.print("수정할 내용 : ");
                    content = sc.next();
                    updateBoards();
                } else if(boardNum == 4) {
                    System.out.print("삭제할 게시글 번호 : ");
                    boardId = sc.nextInt();
                    deleteBoards();
                }

            } else if (selectNum == 2) {
                System.out.println("-------------------------------");
                System.out.println("1.회원 가입 | 2.로그인 | 3.이전메뉴");
                System.out.println("-------------------------------");
                System.out.print("메뉴 선택> ");

                int userNum = sc.nextInt();
                sc.nextLine();

                if (userNum == 1) {
                    System.out.println("회원 가입");
                    System.out.print("아이디 : ");
                    userId = sc.nextLine();
                    System.out.print("패스워드 : ");
                    password = sc.nextLine();
                    System.out.print("연락처 : ");
                    tel = sc.nextLine();
                    join();
                }

                if (userNum == 2) {
                    System.out.println("로그인");
                    System.out.print("아이디 : ");
                    idCheck = sc.nextLine();
                    System.out.print("비밀번호 : ");
                    pwCheck = sc.nextLine();

                    if (login(idCheck, pwCheck)) {
                        System.out.println("로그인 성공");
                    } else {
                        System.out.println("로그인 실패. 아이디나 비밀번호를 확인하세요.");
                    }
                }

                if (userNum == 3) {
                    continue;
                }

            } else if (selectNum == 3) {
                run = false;
            }

            dbExit();
        }
    }
}