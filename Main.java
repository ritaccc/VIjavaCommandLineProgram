package com.vmware.vim.samples;

import com.vmware.vim25.mo.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class Main
{
    // Main function which allowing only three input arguments
    public static void main(String[] args) throws Exception {
        // check if input meets the requirement
        if(args.length != 3) {
            System.out.println("Error: input arguments not fit. Accept IP, userName and password as inputs.");
            return;
        }

        ServiceInstance serviceInstance =
                new ServiceInstance(
                        new URL("https://" + args[0] + "/sdk"),
                        args[1],
                        args[2],
                        true);
        System.out.println("CMPE281 HW2 from Mengxuan Cai");


        Folder rootFolder = serviceInstance.getRootFolder();
        ManagedEntity[] managedEntities = null;
        ManagedEntity[] hostmanagedEntities = null;


        //vm start
        while(true) {
            System.out.print("mengxuan-444> ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine().replaceAll("\\s{2,}", " ").trim();

            if(input.equals("help")) {
                showHelp();
            }
            else if(input.startsWith("host")) {
                hostmanagedEntities = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
                HostHelper.handleHostCmd(input.split(" "), hostmanagedEntities);
            }
            else if(input.startsWith("vm")) {
                managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
                VirtualMachineHelper.handleVMCmd(input.split(" "), managedEntities);

            }
            else if(input.equals("exit")) {
                //exit program
                serviceInstance.getServerConnection().logout();
                return;
            }

            System.out.println();

        }

    }
    // output of help command
    private static void showHelp() {
        System.out.printf("%-20s %s\n", "exit", "exit the program");
        System.out.printf("%-20s %s\n", "host", "enumerate hosts");
        System.out.printf("%-20s %s\n", "host hname info", "show info for hname");
        System.out.printf("%-20s %s\n", "host hname datastore", "enumerate datastores for hname");
        System.out.printf("%-20s %s\n", "host hname network", "enumerate networks for hname");
        System.out.printf("%-20s %s\n", "vm", "enumerate vms");
        System.out.printf("%-20s %s\n", "vm vname info", "show info for vname");
        System.out.printf("%-20s %s\n", "vm vname shutdown", "shutdown OS on vname");
        System.out.printf("%-20s %s\n", "vm vname on", "power on vname");
        System.out.printf("%-20s %s\n", "vm vname off", "power off vname");
    }
}