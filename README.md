# AutoStubTesting

### Compile jar file

- Require install maven
- cd to the project location, paste all command in `precommand` to the cmd/terminal in order to add all local .jar lib to maven dependency
- run `mvn package` to compile source code and get .jar file. In case not want to compile test code, add option `-Dmaven.test.skip=true`. See 2 jar file located in *target* folder

### Run project

- Compile yourself to get .jar file or simply download it in release page
- Run it by command: `java -jar akautauto.jar ` and follow the help on shell
### Khởi tạo môi trường
File\New\C/C++ Enviroment
![image](https://user-images.githubusercontent.com/38599931/202729184-f067564d-1c61-42f1-a6e1-e11df7b6b54b.png)


Thực hiện qua 8 bước như trong hình
* Bước 1: Chọn trình biên dịch phù hợp trong Compilers
![image](https://user-images.githubusercontent.com/38599931/202728686-ecba3745-d5dc-408e-bba4-d547e0c7e54a.png)

* Bước 2: Đặt tên cho môi trường trong Environment Name
![image](https://user-images.githubusercontent.com/38599931/202728631-8216a566-71ae-4ae3-a624-d554f051fceb.png)

* Bước 3: Chọn phương pháp test (testing method)  là Traditional Unit testing
![image](https://user-images.githubusercontent.com/38599931/202728729-408361d3-d4a1-4c9a-a162-f7fe5f0d6215.png)

* Bước 4: Tùy chọn dịch (Build Options): Chọn độ phủ trong Coverage type và các thông số khác
![image](https://user-images.githubusercontent.com/38599931/202728564-6e8344a6-7dff-49a5-ac0c-5319e2ae6d6f.png)

* Bước 5: Chọn project cần test (Locate Source Files): Chọn biểu tượng  hai dấu cộng hoặc một dấu cộng để tải project
![image](https://user-images.githubusercontent.com/38599931/202727419-6f2a66ca-3b2e-45ff-9030-3d9f3567acc3.png)

* Bước 6: Chọn sử dụng phương pháp Stub hoặc không Stub (Choose UUTs & Stubs)
![image](https://user-images.githubusercontent.com/38599931/202727973-70cbdb00-1dfc-481c-b6bc-99d6e5fa3179.png)

Muốn Stub, chọn hàm cần Stub sau đó chọn [SBF(All)]. Không Stub thì chọn [Do not stub (All)] 

* Bước 7: Chọn User Code (tùy chọn)
![image](https://user-images.githubusercontent.com/38599931/202729603-f043dba7-d719-4755-841a-a1795ff5f501.png)

Bước 8: Tóm tắt (Summary) tất cả các thông số đã chọn
![image](https://user-images.githubusercontent.com/38599931/202729900-6bb9ea4b-4418-42d3-b030-0c0051ee2d76.png)

Chờ quá trình dịch diễn ra, nếu thành công xuất hiện thông báo [Successful]
![image](https://user-images.githubusercontent.com/38599931/202730136-9e7f5f1f-2035-4332-8365-58e84b80276d.png)

### Sinh dữ liệu kiểm thử
Sau khi khởi tạo môi trường thành công, xuất hiện giao diện, chọn tab Test Case Navigator
![image](https://user-images.githubusercontent.com/38599931/202730517-1b6d5caa-ca16-4f45-90f4-50c28070441c.png)

Nhấn phải vào hàm cần sinh dữ liệu kiểm thử, chọn Generate test data automatically
![image](https://user-images.githubusercontent.com/38599931/202734150-be354264-28f7-4471-ae0a-7d902e1a3110.png)

Màn hình xuất hiện thông báo về thời gian sinh test data và số bộ dữ liệu kiểm thử sinh ra. Trên màn hình xuất hiện độ phủ cả tệp (Source code) và độ phủ hàm (Function).
![image](https://user-images.githubusercontent.com/38599931/202732430-cf96b082-2517-4b9f-94bf-b464b6f0f52b.png)
Xem test case vừa tạo ra, nhấn chuột phải vào tên test case, chọn Open Test case
![image](https://user-images.githubusercontent.com/38599931/202734499-5676f8f4-7c8d-4116-b211-80acc87c6b25.png)
Muốn chạy test case vừa sinh ra, nhấn phải chuột vào test case cần chạy, chọn Excute
![image](https://user-images.githubusercontent.com/38599931/202734957-ef62f9cb-f778-4a30-a18a-868b5adae87e.png)

Xuất hiện thông báo kết quả
![image](https://user-images.githubusercontent.com/38599931/202734588-aea94df7-85e7-4d99-831c-b1fc424bf7e0.png)

Cuối cùng, muốn xem test driver và test path sinh ra, chọn View test driver (after excution) hoặc View test path (after excution) tương ứng
![image](https://user-images.githubusercontent.com/38599931/202735833-8a5308b0-af0e-4b3b-a95a-4ef7ce6c36a1.png)












