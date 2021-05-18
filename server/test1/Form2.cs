using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using MySql.Data.MySqlClient;

namespace test1
{
    public partial class Form2 : Form
    {
        public Form2()
        {
            InitializeComponent();
        }

        string product_id;

        private void button1_Click(object sender, EventArgs e)
        {
            try
            {
                string cs = @"server=localhost;userid=root;password=12ab!@34cd#$;database=pos";
                using var con = new MySqlConnection(cs);
                con.Open();
                // ex) UPDATE products SET count = count - n WHERE id = xxxx
                var sql = "UPDATE products SET count = count - " + textBox1.Text + " WHERE id = '" + Form1.product_id + "'";
                MessageBox.Show(sql);
                using var cmd = new MySqlCommand(sql, con);
                cmd.ExecuteNonQuery();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private Form1 otherForm;
        private void GetOtherFormTextBox()
        {
            product_id = otherForm.textBox2.Text;
        }
    }
}
