# Chú giải Database

Database: PostgreSQL (`oplearn`), quản lý schema bằng Liquibase — changelog tại `src/main/resources/db/master.xml`, mỗi bảng một file trong `src/main/resources/db/changelog/`.

Dữ liệu thơ lấy từ dataset [vietnamese-poetry-corpus](https://huggingface.co/datasets/phamson02/vietnamese-poetry-corpus) (198.598 bài, nguồn thivien.net, license CC-BY-4.0).

## Các cột chung (mọi bảng)

Tất cả các bảng nghiệp vụ đều kế thừa `BaseEntity` (`org.oplearn.project.entity.base.BaseEntity`) nên có chung các cột sau:

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | `bigint`, tự tăng, PK | Định danh nội bộ, sinh tự động khi insert. Không phải id từ nguồn dữ liệu. |
| `is_deleted` | `boolean`, NOT NULL, mặc định `false` | Cờ xóa mềm (soft delete). `true` = đã xóa về mặt nghiệp vụ nhưng vẫn còn trong DB. Mọi query nghiệp vụ phải lọc `is_deleted = false`. |
| `created_by` | `varchar(255)` | Username người tạo bản ghi, do Spring Data Auditing tự điền. |
| `created_at` | `timestamptz` | Thời điểm tạo, tự điền, không cập nhật lại. |
| `updated_by` | `varchar(255)` | Username người sửa lần cuối, tự điền. |
| `updated_at` | `timestamptz` | Thời điểm sửa lần cuối, tự điền. |

## Bảng `users`

Tài khoản đăng nhập hệ thống. Changeset: `001-create-table-users.xml`.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `username` | `varchar(255)`, NOT NULL | Tên đăng nhập. Có index `idx_users_username`. |
| `password` | `varchar(255)`, NOT NULL | Mật khẩu đã băm (BCrypt). Không bao giờ lưu plain text. |
| `name` | `varchar(255)` | Tên hiển thị. |
| `email` | `varchar(255)` | Email liên hệ. Có index `idx_users_email`. |

## Bảng `authors`

Danh mục tác giả thơ, tách chuẩn hóa từ cột `author` của dataset gốc. Changeset: `002-create-table-authors.xml`.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `name` | `varchar(255)`, NOT NULL, UNIQUE (`uk_authors_name`) | Tên tác giả, ví dụ "Phạm Thái", "Nguyễn Bính". Là khóa tự nhiên để map dữ liệu khi import. |

Lưu ý: dataset gốc chỉ có ~36.900/198.598 bài (19%) ghi rõ tác giả, nên bảng này chỉ chứa các tác giả xác định được.

## Bảng `genres`

Danh mục thể loại thơ, tách chuẩn hóa từ cột `genre` của dataset gốc. Changeset: `003-create-table-genres.xml`.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `name` | `varchar(255)`, NOT NULL, UNIQUE (`uk_genres_name`) | Tên thể loại. |

Dataset gốc có đúng 7 thể loại (kèm số bài): lục bát (89.943), bảy chữ (46.586), tám chữ (34.920), thơ tự do (14.316), năm chữ (10.185), bốn chữ (1.970), sáu chữ (678).

## Bảng `poems`

Bài thơ — bảng dữ liệu chính. Changeset: `004-create-table-poems.xml`.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `title` | `text`, nullable | Tiêu đề bài thơ. ~31% bài trong dataset không có tiêu đề → NULL. |
| `content` | `text`, NOT NULL | Nội dung bài thơ, các câu ngăn cách bằng ký tự xuống dòng `\n`. Dấu ngắt dòng `<` / `>` của dataset gốc đã được chuyển thành `\n` khi import. Văn bản đã được tokenize sẵn (có khoảng trắng trước dấu câu, ví dụ `trời ,`). Bài dài nhất ~67.000 ký tự. |
| `source_url` | `text`, nullable | Link bài gốc trên thivien.net. **Không unique** — 2.694 URL xuất hiện ở nhiều bản ghi (cùng một bài được xếp vào nhiều thể loại), vì vậy không dùng làm khóa. Có index `idx_poems_source_url` để tra ngược. |
| `period` | `varchar(255)`, nullable | Thời kỳ văn học (ví dụ "hiện đại", "trung đại"). ~81% bài không có thông tin này → NULL. |
| `specific_genre` | `varchar(255)`, nullable | Thể loại chi tiết hơn từ nguồn (phân loại phụ của thivien.net), giữ nguyên dạng text vì giá trị tự do, không đáng tách bảng. |
| `author_id` | `bigint`, FK → `authors(id)` (`fk_poems_author`), nullable | Tác giả. NULL khi dataset gốc không ghi tác giả (~81% số bài). Có index `idx_poems_author_id`. |
| `genre_id` | `bigint`, FK → `genres(id)` (`fk_poems_genre`), nullable | Thể loại chính (1 trong 7 giá trị). Có index `idx_poems_genre_id`. |

## Quan hệ giữa các bảng

```
authors 1 ──── n poems n ──── 1 genres
```

- Một tác giả có nhiều bài thơ; một bài thơ thuộc tối đa một tác giả (`poems.author_id`, nullable).
- Một thể loại có nhiều bài thơ; một bài thơ thuộc tối đa một thể loại chính (`poems.genre_id`, nullable).
- `users` độc lập, không quan hệ với các bảng thơ.

## Bảng hệ thống (không đụng vào)

| Bảng | Ý nghĩa |
|---|---|
| `databasechangelog` | Liquibase ghi lại các changeset đã chạy. Không sửa tay. |
| `databasechangeloglock` | Khóa chống hai tiến trình cùng chạy migration. Không sửa tay. |

## Quy ước khi thêm bảng mới

1. Tạo file `NNN-create-table-<tên>.xml` trong `db/changelog/` (NNN tăng dần, changeset id trùng tên file, author `oplearn-base`).
2. Thêm `<include>` vào `db/master.xml` theo đúng thứ tự.
3. Luôn có đủ cột chung (`id`, `is_deleted`, 4 cột audit) để entity kế thừa được `BaseEntity`.
4. Không sửa changeset đã chạy trên môi trường khác — tạo changeset mới để alter.
