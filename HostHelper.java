package com.vmware.vim.samples;

import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostListSummary;
import com.vmware.vim25.mo.*;

import java.rmi.RemoteException;
//helper class fpr HostHelper host related commands
public class HostHelper {
    //handle different host command
    public static void handleHostCmd(String[] input, ManagedEntity[] hostmanagedEntities) {

        if (input.length == 1 && "host".equals(input[0])) {
            enumerateHosts(hostmanagedEntities);
        }
        else if (input.length == 3) {
            if("info".equals(input[2])) {
                showHostInfo(input[1], hostmanagedEntities);
            }
            else if("datastore".equals(input[2])) {
                try {
                    showHostDataStore(input[1], hostmanagedEntities);
                } catch (RemoteException e) {
                    System.out.println("Error: DataStore can't find");
                }
            }
            else if("network".equals(input[2])) {
                try {
                    showHostNetwork(input[1], hostmanagedEntities);
                } catch (RemoteException e) {
                    System.out.println("Error: Network can't find");
                }
            }
        }
        else {
            System.out.println("Warning: Command not correct! Check command help.");
        }

    }
    // output of host command
    private static void enumerateHosts(ManagedEntity[] hostmanagedEntities) {
        for (int i = 0; i<hostmanagedEntities.length;i++) {
            ManagedEntity managedEntity = hostmanagedEntities[i];
            HostSystem hostsystemobj = (HostSystem) managedEntity;
            System.out.println("host[" + i + "]: Name = " + hostsystemobj.getName());
        }
    }
    // output of host hname info command
    private static void showHostInfo(String hostName, ManagedEntity[] hostmanagedEntities) {
        //Get target host from input
        HostSystem hostsystemobj = getTargetHost(hostName, hostmanagedEntities);

        if(hostsystemobj == null) {
            System.out.println("Failed: Invalid host name = " + hostName);
            return;
        }
        System.out.println("Host:");
        System.out.printf("%10s%s %s\n", "", "Name =", hostsystemobj.getName());

        HostListSummary summary = hostsystemobj.getSummary();
        HostConfigInfo config = hostsystemobj.getConfig();
        String productFullName = config.getProduct().getFullName();
        System.out.printf("%10s%s %s\n", "", "ProductFullName =", productFullName);

        short ESXhostcores = summary.getHardware().getNumCpuCores();
        System.out.printf("%10s%s %d\n", "", "Cpu cores =", ESXhostcores);

        long ESXhostmem = summary.getHardware().getMemorySize()/1024/1024/1024;
        System.out.printf("%10s%s %d %s\n", "", "RAM =", ESXhostmem, "GB");

    }
    // output of host hname datastore command
    private static void showHostDataStore(String hostName, ManagedEntity[] hostmanagedEntities)
            throws RemoteException {
        //Get target host from input
        HostSystem hostsystemobj = getTargetHost(hostName, hostmanagedEntities);

        if(hostsystemobj == null) {
            System.out.println("Failed: Invalid host name = " + hostName);
            return;
        }

        System.out.println("Host:");
        System.out.printf("%10s%s %s\n", "", "Name =", hostsystemobj.getName());
        //extract datastores from host
        HostDatastoreSystem hds = hostsystemobj.getHostDatastoreSystem();
        Datastore[] datastores = hds.getDatastores();

        for(int i = 0; i< datastores.length; i++) {
            System.out.printf("%10s", "");
            System.out.print("Datastore[" + i +"]: ");
            Datastore datastore = datastores[i];
            String name = datastore.getName();
            System.out.print("name = " + name);
            long cap = datastore.getSummary().getCapacity()/1024/1024/1024;
            System.out.print(", capacity = " + cap + " GB");
            long freeSpace = datastore.getSummary().getFreeSpace()/1024/1024/1024;
            System.out.println(", FreeSpace = " + freeSpace + " GB.");
        }
    }
    //output of host hname network command
    private static void showHostNetwork(String hostName, ManagedEntity[] hostmanagedEntities) throws RemoteException {
        //Get target host from input
        HostSystem hostsystemobj = getTargetHost(hostName, hostmanagedEntities);

        if(hostsystemobj == null) {
            System.out.println("Failed: Invalid host name = " + hostName);
            return;
        }

        System.out.println("Host:");
        System.out.printf("%10s%s %s\n", "", "Name =", hostsystemobj.getName());

        Network[] networks = hostsystemobj.getNetworks();
        for(int i = 0; i< networks.length; i++) {
            System.out.printf("%10s", "");
            System.out.print("Network[" + i +"]: ");
            Network network = networks[i];
            String name = network.getName();
            System.out.print("name = " + name);
        }
    }
    //get target host
    private static HostSystem getTargetHost(String hostName, ManagedEntity[] hostmanagedEntities) {
        HostSystem host = null;
        if (hostName != null) {
            for (int i = 0; i<hostmanagedEntities.length;i++) {
                ManagedEntity managedEntity = hostmanagedEntities[i];
                HostSystem hostsystemobj = (HostSystem) managedEntity;
                if (hostName.equals(hostsystemobj.getName())) {
                    host = hostsystemobj; break;
                }
            }
        }
        return host;
    }
}
