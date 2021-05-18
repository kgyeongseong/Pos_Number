using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.IO;

using MySql.Data.MySqlClient;

namespace test1
{
    public partial class Form1 : Form
    {
        // 클라이언트의 접속요청을 받아들일 객체
        TcpListener tcpListener = null;

        public static Form1 form1;

        byte[] buff;
        int nbytes;

        public static string product_id;

        public Form1()
        {
            InitializeComponent();
            form1 = this;
        }

        // 프로그램이 실행되고, 폼이 로드될 때 가장 먼저 호출되는 함수
        // 서버컴퓨터의 아이피를 받아오는 역할
        private void Form1_Load(object sender, EventArgs e)
        {
            tcpListener = new TcpListener(3333);
            tcpListener.Start();

            // 서버의 아이피를 받아온다
            IPHostEntry host = Dns.GetHostEntry(Dns.GetHostName());

            // 위에서 받아온 서버의 아이피에서, IPv4에 해당하는 아이피 주소를 걸러낸다
            for (int i = 0; i < host.AddressList.Length; i++)
            {
                if (host.AddressList[i].AddressFamily == AddressFamily.InterNetwork)
                {
                    textBox1.Text = host.AddressList[i].ToString();
                    break;
                }
            }
            tcpListener.Stop();

            
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Thread th = new Thread(new ThreadStart(RunTcpServer));
            th.IsBackground = true;
            th.Start();
        }

        private void button2_Click(object sender, EventArgs e)
        {
            textBox2.Text = Encoding.UTF8.GetString(buff);
            product_id = textBox2.Text;
        }

        private void button3_Click(object sender, EventArgs e)
        {
            Form2 form2 = new Form2();
            form2.Show();
        }

        // 클라이언트의 연결요청을 대기하는 함수 (쓰레드로 돌려질 함수)
        void RunTcpServer()
        {
            // (1) 로컬 포트 3333 을 Listen
            TcpListener listener = new TcpListener(IPAddress.Any, 3333);
            listener.Start();

            //byte[] buff = new byte[1024];
            buff = new byte[1024];

            while (true)
            {
                // (2) TcpClient Connection 요청을 받아들여
                //     서버에서 새 TcpClient 객체를 생성하여 리턴
                TcpClient tc = listener.AcceptTcpClient();

                // (3) TcpClient 객체에서 NetworkStream을 얻어옴 
                NetworkStream stream = tc.GetStream();

                // (4) 클라이언트가 연결을 끊을 때까지 데이타 수신
                //int nbytes;
                while ((nbytes = stream.Read(buff, 0, buff.Length)) > 0)
                {
                    // (5) 데이타 그대로 송신
                    stream.Write(buff, 0, nbytes);
                    //form1.textBox2.Text += buff.ToString();
                    //UnSafeTextBox(Encoding.UTF8.GetString(buff, 0, nbytes));
                    
                }

                //Thread th = new Thread(run);
                //th.Start();

                // (6) 스트림과 TcpClient 객체 
                stream.Close();
                tc.Close();

                }

                // (7) 계속 반복
            }

        private delegate void DelegateSetTextBox(object msg);
        private void UnSafeTextBox(object text)
        {
            DelegateSetTextBox d = (msg) => { textBox2.Text = (string)msg; };
            this.textBox2.Invoke(d, new object[] { text });
        }
        private void changeTextBox()
        {
            Form1 form = new Form1();
            form.textBox2.Text = "what";
        }
        delegate void testDel();
        void test()
        {
            textBox2.Text = Encoding.UTF8.GetString(buff, 0, nbytes);
        }
        void run()
        {
            testDel td = new testDel(test);

            if (this.textBox2.InvokeRequired)
            {
                this.Invoke(td);
            }
            else
            {
                test();
            }
        }
    }


    }
